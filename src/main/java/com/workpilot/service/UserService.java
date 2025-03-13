package com.workpilot.service;

import com.workpilot.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> GetAllUsers();
    User getUserById(Long id);
    User createUser(User user);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
}
