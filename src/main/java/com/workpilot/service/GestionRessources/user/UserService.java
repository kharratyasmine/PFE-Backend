package com.workpilot.service.GestionRessources.user;

import com.workpilot.dto.GestionRessources.UserDTO;
import com.workpilot.entity.auth.ChangePasswordRequest;
import com.workpilot.entity.auth.User;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserDTO> getAllUsers();
    User getUserById(Long id);
    User createUser(User user);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    Optional<User> findByEmail(String email);
    void changePassword(ChangePasswordRequest request, Principal connectedUser);
    UserDTO getUserDtoByEmail(String email);
    UserDTO updateMyProfile(UserDTO userDTO, Principal connectedUser);
    UserDTO updateMyProfileWithPhoto(UserDTO dto, MultipartFile photo, Principal principal);

}
