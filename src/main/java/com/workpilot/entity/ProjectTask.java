package com.workpilot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tasks")
public class ProjectTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String statut; // État de la tâche (TODO, IN_PROGRESS, DONE)
    private int progress;


    // Une tâche appartient à un projet
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;


    // Une tâche peut être assignée à une ressource spécifique
    @ManyToOne
    @JoinColumn(name = "teamMember_id", nullable = true)
    private TeamMember teamMember;
}
