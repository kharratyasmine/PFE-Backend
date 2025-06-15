package com.workpilot.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
public class NotificationDTO {
    private Long id;
    private String title;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
    private String roleTargeted;

    // Constructors, Getters, Setters
    public NotificationDTO(Long id, String title, String message, boolean read, LocalDateTime createdAt, String roleTargeted) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
        this.roleTargeted = roleTargeted;
    }

    // Getters and Setters (ou @Data avec Lombok)
}
