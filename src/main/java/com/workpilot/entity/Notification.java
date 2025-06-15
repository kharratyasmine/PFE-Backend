package com.workpilot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String message;
    @Column(name = "is_read")
    private boolean isRead;


    private LocalDateTime createdAt;

    private String roleTargeted; // ex: ADMIN, QUALITE, etc.
}
