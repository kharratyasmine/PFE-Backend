package com.workpilot.authentification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workpilot.configuration.JwtService;
import com.workpilot.entity.auth.Role;
import com.workpilot.entity.auth.User;
import com.workpilot.entity.auth.token.ApprovalStatus;
import com.workpilot.entity.auth.token.JwtTokenType;
import com.workpilot.entity.auth.token.Token;
import com.workpilot.entity.auth.token.TokenRepository;
import com.workpilot.repository.ressources.UserRepository;
import com.workpilot.service.EmailService;
import com.workpilot.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final Map<String, Integer> loginAttempts = new ConcurrentHashMap<>();
    private final Map<String, Long> lockTime = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION = 5 * 60 * 1000; // 5 minutes
    private final NotificationService notificationService;

    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .approvalStatus(
                        request.getRole().equals(Role.ADMIN) ? ApprovalStatus.APPROVED : ApprovalStatus.PENDING
                )
                .build();

        var savedUser = repository.save(user);

        if (user.getApprovalStatus() == ApprovalStatus.PENDING) {
            Long userId = savedUser.getId();
            String validationLink = "http://localhost:4200/validate-user?userId=" + userId;
            String rejectionLink = validationLink + "&reject=true";

            String emailContent = """
  <div style='font-family: Arial, sans-serif;'>
    <h2 style='color: #2c3e50;'>🔐 Nouvel utilisateur à valider</h2>
    <p><strong>Email :</strong> %s</p>
    <p><strong>Rôle demandé :</strong> %s</p>
    <p>
      <a href='%s' style='
          display: inline-block;
          margin-right: 10px;
          padding: 10px 20px;
          background-color: green;
          color: white;
          text-decoration: none;
          border-radius: 5px;'>✅ Accepter</a>

      <a href='%s' style='
          display: inline-block;
          padding: 10px 20px;
          background-color: red;
          color: white;
          text-decoration: none;
          border-radius: 5px;'>❌ Refuser</a>
    </p>
  </div>
""".formatted(
                    user.getEmail(),
                    user.getRole(),
                    "http://localhost:4200/validate-user?userId=" + savedUser.getId(),
                    "http://localhost:4200/validate-user?userId=" + savedUser.getId() + "&reject=true"
            );


            emailService.sendToAdmins(
                    "🔐 Validation d’un nouveau compte",
                    emailContent
            );
// 🔔 Notification WebSocket
            notificationService.sendNotification("👤 Nouvelle demande d’inscription : " + user.getEmail());
            return AuthenticationResponse.builder()
                    .message("Votre inscription est en attente de validation par un administrateur.")
                    .build();
        }


        // 🎯 Cas d’un admin → activation immédiate
        var jwtToken = jwtService.generateToken(savedUser);
        var refreshToken = jwtService.generateRefreshToken(savedUser);
        saveUserToken(savedUser, jwtToken);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var userOptional = repository.findByEmail(request.getEmail());

        // 1️⃣ Vérifie si l'utilisateur existe
        if (userOptional.isEmpty()) {
            throw new IllegalStateException("❌ Email introuvable !");
        }

        var user = userOptional.get();

        // 2️⃣ Vérifie le statut d'approbation
        if (user.getApprovalStatus() == ApprovalStatus.PENDING) {
            throw new IllegalStateException("⏳ Votre compte est en attente de validation par un administrateur.");
        }
        if (user.getApprovalStatus() == ApprovalStatus.REJECTED) {
            throw new IllegalStateException("🚫 Votre demande d'inscription a été refusée.");
        }

        // 3️⃣ Vérifie si le compte est temporairement bloqué
        if (isBlocked(request.getEmail())) {
            throw new IllegalStateException("🚫 Trop de tentatives échouées ! Compte temporairement bloqué. Réessayez plus tard.");
        }

        // 4️⃣ Vérifie le mot de passe
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            increaseLoginAttempts(request.getEmail());
            throw new IllegalStateException("❌ Mot de passe incorrect !");
        }

        // 5️⃣ Réinitialise le compteur après succès
        resetLoginAttempts(request.getEmail());

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }


    private void increaseLoginAttempts(String email) {
        loginAttempts.put(email, loginAttempts.getOrDefault(email, 0) + 1);

        if (loginAttempts.get(email) >= MAX_ATTEMPTS) {
            lockTime.put(email, System.currentTimeMillis()); // 🔒 Bloquer l'utilisateur
        }
    }

    private boolean isBlocked(String email) {
        if (!lockTime.containsKey(email)) {
            return false;
        }

        long lockTimeElapsed = System.currentTimeMillis() - lockTime.get(email);
        if (lockTimeElapsed > LOCK_DURATION) {
            resetLoginAttempts(email); // 🔄 Débloquer après 5 minutes
            return false;
        }
        return true;
    }

    private void resetLoginAttempts(String email) {
        loginAttempts.remove(email);
        lockTime.remove(email);
    }


    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(JwtTokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            var user = this.repository.findByEmail(userEmail)
                    .orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }
}
