package com.workpilot.service.GestionRessources.user;

import com.workpilot.dto.GestionRessources.UserDTO;
import com.workpilot.entity.auth.ChangePasswordRequest;
import com.workpilot.entity.auth.Role;
import com.workpilot.entity.auth.User;

import com.workpilot.repository.ressources.UserRepository;
import com.workpilot.service.GestionRessources.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public UserServiceImpl(UserRepository userRepository ,PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder =passwordEncoder;
    }

    @Override
    public void changePassword(ChangePasswordRequest request, Principal connectedUser) {

        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        // check if the current password is correct
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalStateException("Wrong password");
        }
        // check if the two new passwords are the same
        if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
            throw new IllegalStateException("Password are not the same");
        }


        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    UserDTO dto = new UserDTO();
                    dto.setId(user.getId());
                    dto.setFirstname(user.getFirstname());
                    dto.setLastname(user.getLastname());
                    dto.setEmail(user.getEmail());

                    return dto;
                })
                .collect(Collectors.toList());
    }


    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Error: Email already in use");
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(Long id, User user) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    if (!existingUser.getEmail().equals(user.getEmail()) &&
                            userRepository.findByEmail(user.getEmail()).isPresent()) {
                        throw new RuntimeException("Error updating user: Email already in use");
                    }
                    existingUser.setFirstname(user.getFirstname());
                    existingUser.setLastname(user.getLastname());
                    existingUser.setEmail(user.getEmail());
                    existingUser.setPassword(user.getPassword()); // Pas de hashage
                    existingUser.setRole(user.getRole());
                    existingUser.setPhoneNumber(user.getPhoneNumber());
                    existingUser.setAddress(user.getAddress());
                    existingUser.setPhotoUrl(user.getPhotoUrl());

                    return userRepository.save(existingUser);
                }).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserDTO getUserDtoByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
        return convertToDTO(user);
    }


    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstname(user.getFirstname());
        dto.setLastname(user.getLastname());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());
        dto.setPhotoUrl(user.getPhotoUrl());


        return dto;
    }

    @Override
    @Transactional
    public UserDTO updateMyProfile(UserDTO userDTO, Principal connectedUser) {
        String email = connectedUser.getName(); // extrait depuis le token JWT
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        // Mise à jour uniquement des champs autorisés
        user.setFirstname(userDTO.getFirstname());
        user.setLastname(userDTO.getLastname());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setAddress(userDTO.getAddress());
        user.setPhotoUrl(userDTO.getPhotoUrl());
        // ⚠️ pas d’update du mot de passe ou rôle ici pour éviter les abus

        return convertToDTO(userRepository.save(user));
    }

    @Override
    public UserDTO updateMyProfileWithPhoto(UserDTO dto, MultipartFile photo, Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setFirstname(dto.getFirstname());
        user.setLastname(dto.getLastname());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setAddress(dto.getAddress());

        if (photo != null && !photo.isEmpty()) {
            String filename = "member_" + user.getId() + "_" + System.currentTimeMillis() + ".jpg";
            Path path = Paths.get("uploads").resolve(filename);
            try {
                Files.copy(photo.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                user.setPhotoUrl(filename);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save photo", e);
            }
        }

        return convertToDTO(userRepository.save(user));
    }

}