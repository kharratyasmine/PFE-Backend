package com.workpilot.entity.PSR;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class PsrGenerationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId;
    private String projectName;
    private String week;
    private int year;

    private LocalDateTime generatedAt;

    private String status; // "SUCCESS", "ALREADY_EXISTS", "ERROR"
    private String message;
}

