package com.workpilot.controller.GestionRessources;

import com.workpilot.dto.GestionRessources.ApprovalRequest;
import com.workpilot.dto.GestionRessources.UserDTO;
import com.workpilot.entity.auth.ChangePasswordRequest;
import com.workpilot.entity.auth.User;

import com.workpilot.entity.auth.token.ApprovalStatus;
import com.workpilot.repository.ressources.UserRepository;
import com.workpilot.service.EmailService;
import com.workpilot.service.GestionRessources.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {


    private final UserService userService;
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private EmailService emailService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            return ResponseEntity.ok(userService.getAllUsers());
        } catch (Exception e) {
            e.printStackTrace(); // très important pour voir l’erreur dans la console Spring
            return ResponseEntity.status(500).body("Erreur interne : " + e.getMessage());
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PatchMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            Principal connectedUser
    ) {
        userService.changePassword(request, connectedUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        UserDTO userDTO = userService.getUserDtoByEmail(email);
        return ResponseEntity.ok(userDTO);
    }


    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateMyProfile(@Valid @RequestBody UserDTO userDTO, Principal principal) {
        UserDTO updated = userService.updateMyProfile(userDTO, principal);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/me/general-infos")
    public ResponseEntity<UserDTO> updateGeneralInfos(
            @RequestPart("data") UserDTO userDTO,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            Principal principal
    ) {
        try {
            UserDTO updated = userService.updateMyProfileWithPhoto(userDTO, photo, principal);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace(); // Log en console
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @PatchMapping("/{id}/approval")
    @PreAuthorize("hasRole('ADMIN')")  // Seuls les admins peuvent approuver/refuser
    public ResponseEntity<?> approveUser(
            @PathVariable Long id,
            @RequestBody ApprovalRequest request) {

        Logger logger = LoggerFactory.getLogger(UserController.class);
        logger.info("\n=== DÉBUT DU TRAITEMENT D'APPROBATION ===");
        logger.info("ID utilisateur: {}", id);
        logger.info("Requête reçue: {}", request);

        try {
            User user = userRepository.findById(id)

                    .orElseThrow(() -> {
                        logger.error("❌ Utilisateur non trouvé pour l'ID: {}", id);
                        return new RuntimeException("Utilisateur non trouvé");
                    });

            logger.info("✅ Utilisateur trouvé: {}", user.getEmail());
            logger.info("Statut actuel: {}", user.getApprovalStatus());

            if (request.isApproved()) {
                logger.info("🔄 Passage du statut à APPROVED");
                user.setApprovalStatus(ApprovalStatus.APPROVED);
                user.setRejectionReason(null);

                String loginUrl = "http://localhost:4200/login";
                String content = """
    <div style='font-family:Arial, sans-serif;'>
      <h2 style='color:#2ecc71;'>🎉 Votre compte a été validé</h2>
      <p>Bonjour %s,</p>
      <p>Votre inscription a été approuvée avec succès. Vous pouvez maintenant accéder à la plateforme :</p>
      <p>
        <a href="%s" style="
          display:inline-block;
          padding:10px 20px;
          background-color:#3498db;
          color:white;
          text-decoration:none;
          border-radius:5px;">
          🔐 Se connecter
        </a>
      </p>
      <br/>
      <p style='color:#7f8c8d;'>Cordialement,<br/>L'équipe WorkLPilot</p>
    </div>
""".formatted(user.getFirstname(), loginUrl);

                emailService.send(user.getEmail(), "✅ Votre compte est activé", content);
                logger.info("✉️ Email d'approbation envoyé à {}", user.getEmail());

            } else {
                logger.info("🔄 Passage du statut à REJECTED");
                user.setApprovalStatus(ApprovalStatus.REJECTED);
                user.setRejectionReason(request.getReason());
                logger.info("Raison du rejet: {}", request.getReason());

                String content = """
    <div style='font-family:Arial, sans-serif;'>
      <h2 style='color:#e74c3c;'>❌ Demande refusée</h2>
      <p>Bonjour %s,</p>
      <p>Nous sommes désolés, votre demande d'inscription a été refusée.</p>
      <p><strong>Raison :</strong> %s</p>
      <p style='color:#7f8c8d;'>Cordialement,<br/>L'équipe WorkLPilot</p>
    </div>
""".formatted(user.getFirstname(), request.getReason());

                emailService.send(user.getEmail(), "❌ Demande refusée", content);
                logger.info("✉️ Email de rejet envoyé à {}", user.getEmail());
            }

            logger.info("Tentative de sauvegarde de l'utilisateur...");
            User savedUser = userRepository.save(user);
            logger.info("✅ Utilisateur sauvegardé avec succès. Nouveau statut: {}", savedUser.getApprovalStatus());

            // Vérification après sauvegarde
            User verifyUser = userRepository.findById(id).orElse(null);
            logger.info("Vérification après sauvegarde - Statut: {}", verifyUser != null ? verifyUser.getApprovalStatus() : "Utilisateur non trouvé");

            logger.info("=== FIN DU TRAITEMENT D'APPROBATION ===\n");
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            logger.error("❌ ERREUR CRITIQUE: {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du traitement: " + e.getMessage());
        }
    }

    @GetMapping("/test")
    public String test() {
        return "✅ UserController fonctionne";
    }

}