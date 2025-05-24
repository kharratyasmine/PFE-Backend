package com.workpilot.controller.GestionRessources;

import com.workpilot.dto.GestionRessources.ClientDTO;
import com.workpilot.dto.GestionRessources.ProjectDTO;
import com.workpilot.entity.auth.Role;
import com.workpilot.entity.auth.User;
import com.workpilot.entity.ressources.Client;
import com.workpilot.repository.ressources.ClientRepository;
import com.workpilot.service.GestionRessources.client.ClientService;
import io.swagger.annotations.Authorization;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/clients")
@CrossOrigin(origins = "http://localhost:4200")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientRepository clientRepository;

    // Récupérer tous les clients
    @GetMapping
    public List<ClientDTO> getAllClients(@AuthenticationPrincipal User user) {
        Role role = user.getRole();

        return clientRepository.findAll().stream()
                .map(client -> {
                    ClientDTO dto = new ClientDTO();

                    // ✅ Ajout de l'ID manquant
                    dto.setId(client.getId());
                    dto.setCompany(client.getCompany());
                    dto.setSalesManagers(client.getSalesManagers());

                    // Champs visibles uniquement pour QUALITE et ADMIN
                    if (role == Role.ADMIN || role == Role.COORDINATEUR_QUALITE || role == Role.RESPONSABLE_QUALITE || role == Role.RESPONSABLE_PROJET) {
                        dto.setEmail(client.getEmail());
                    }

                    // Champs visibles uniquement pour ADMIN
                    if (role == Role.ADMIN) {
                        dto.setContact(client.getContact());
                        dto.setAddress(client.getAddress());
                    }

                    return dto;
                })
                .sorted(Comparator.comparing(ClientDTO::getId)) // ✅ Tri par ID croissant
                .collect(Collectors.toList());
    }


    // Création client avec DTO
    @PostMapping
    public ResponseEntity<?> createClient(@Valid @RequestBody ClientDTO clientDTO) {
        try {
            Client savedClient = clientService.saveClient(clientDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedClient);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'ajout : " + e.getMessage());
        }
    }



    // Mise à jour client avec DTO
    @PutMapping("/{id}")
    public ResponseEntity<?> updateClient(@PathVariable Long id, @Valid @RequestBody ClientDTO clientDTO) {
        try {
            Client updatedClient = clientService.updateClient(id, clientDTO);
            return ResponseEntity.ok(updatedClient); // ✅ Retourne le client mis à jour
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // ✅ Retourne une erreur 404 si client non trouvé
        }
    }

    // Supprimer un client
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        try {
            clientService.deleteClient(id);
            return ResponseEntity.noContent().build(); // ✅ Pas de contenu à retourner après suppression
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build(); // ✅ Retourne 404 si client non trouvé
        }
    }

    @GetMapping("/{id}/projects")
    public ResponseEntity<List<ProjectDTO>> getProjectsByClient(@PathVariable Long id) {
        try {
            List<ProjectDTO> projects = clientService.getProjectsByClient(id);
            return ResponseEntity.ok(projects); // ✅ Retourne les projets du client sous forme de DTO
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build(); // ✅ Retourne 404 si aucun projet trouvé pour ce client
        }
    }


    // Méthode utilitaire privée pour conversion DTO -> Entity
    private Client convertToEntity(ClientDTO dto) {
        Client client = new Client();
        client.setCompany(dto.getCompany());
        client.setSalesManagers(dto.getSalesManagers());
        client.setContact(dto.getContact());
        client.setAddress(dto.getAddress());
        client.setEmail(dto.getEmail());
        return client;
    }
}
