package com.workpilot.authentification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workpilot.configuration.JwtService;
import com.workpilot.entity.auth.User;
import com.workpilot.entity.auth.token.JwtTokenType;
import com.workpilot.entity.auth.token.Token;
import com.workpilot.entity.auth.token.TokenRepository;
import com.workpilot.repository.ressources.UserRepository;
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
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final Map<String, Integer> loginAttempts = new ConcurrentHashMap<>();
    private final Map<String, Long> lockTime = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION = 5 * 60 * 1000; // 5 minutes

    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
        var savedUser = repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var user = repository.findByEmail(request.getEmail());

        // 🔴 Vérifie d'abord si l'email existe en base de données
        if (user.isEmpty()) {
            throw new IllegalStateException("❌ Email introuvable !");
        }

        // 🔴 Vérifie si le compte est bloqué après trop de tentatives
        if (isBlocked(request.getEmail())) {
            throw new IllegalStateException("🚫 Trop de tentatives échouées ! Compte temporairement bloqué. Réessayez après 5 minutes.");
        }

        // 🔴 Vérifie si le mot de passe est correct
        boolean isPasswordValid = passwordEncoder.matches(request.getPassword(), user.get().getPassword());
        if (!isPasswordValid) {
            increaseLoginAttempts(request.getEmail()); // 🔼 Incrémente les tentatives en cas d'échec
            throw new IllegalStateException("❌ Mot de passe incorrect !");
        }

        // 🔥 Réinitialiser le compteur de tentatives après une connexion réussie
        resetLoginAttempts(request.getEmail());

        var authenticatedUser = user.get();
        var jwtToken = jwtService.generateToken(authenticatedUser);
        var refreshToken = jwtService.generateRefreshToken(authenticatedUser);
        revokeAllUserTokens(authenticatedUser);
        saveUserToken(authenticatedUser, jwtToken);

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
