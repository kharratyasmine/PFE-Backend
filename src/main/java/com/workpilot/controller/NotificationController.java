package com.workpilot.controller;

import com.workpilot.entity.Notification;
import com.workpilot.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(Authentication authentication) {
        String userRole = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("");
        List<Notification> notifications = notificationRepository.findAll().stream()
                .filter(n -> n.getRoleTargeted() == null || n.getRoleTargeted().equalsIgnoreCase(userRole))
                .collect(Collectors.toList());
        List<NotificationDTO> dtos = notifications.stream()
                .map(n -> new NotificationDTO(n.getId(), n.getTitle(), n.getMessage(), n.isRead(), n.getCreatedAt(), n.getRoleTargeted()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
        return ResponseEntity.ok().build();
    }

    public static class NotificationDTO {
        public Long id;
        public String title;
        public String message;
        public boolean read;
        public LocalDateTime createdAt;
        public String roleTargeted;

        public NotificationDTO(Long id, String title, String message, boolean read, LocalDateTime createdAt, String roleTargeted) {
            this.id = id;
            this.title = title;
            this.message = message;
            this.read = read;
            this.createdAt = createdAt;
            this.roleTargeted = roleTargeted;
        }
    }
}