package com.workpilot.controller.GestionRessources;

import com.workpilot.dto.GestionRessources.UserDTO;
import com.workpilot.entity.auth.ChangePasswordRequest;
import com.workpilot.entity.auth.User;

import com.workpilot.service.GestionRessources.user.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
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


}