package com.workpilot.service.GestionProject.user;

import com.workpilot.entity.auth.ChangePasswordRequest;
import com.workpilot.entity.auth.User;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAllUsers();
    User getUserById(Long id);
    User createUser(User user);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    Optional<User> findByEmail(String email);
    void changePassword(ChangePasswordRequest request, Principal connectedUser);
    User getUserByEmail(String email);
}
