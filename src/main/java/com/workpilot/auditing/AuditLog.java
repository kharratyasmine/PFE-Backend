package com.workpilot.auditing;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String action;
    private String entityAffected;
    private String methodName;
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String parameters;


}

