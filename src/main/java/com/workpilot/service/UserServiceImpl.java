package com.workpilot.service;

import com.workpilot.entity.User;
import com.workpilot.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {


    private final  UserRepository userRepository;
    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public List<User> GetAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    public User createUser(User user) {
        // Enregistrer l'utilisateur
        User savedUser = userRepository.save(user);
              return savedUser;
    }

    @Transactional
    public User updateUser(Long id, User user) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setFirstname(user.getFirstname());
                     existingUser.setLastname(user.getLastname());
                     existingUser.setEmail(user.getEmail());
                     existingUser.setPassword(user.getPassword());
                     existingUser.setRole(user.getRole());
                     return userRepository.save(existingUser);
        })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

}
