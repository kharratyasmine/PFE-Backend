package com.workpilot.entity.Dashboard;

import com.workpilot.entity.ressources.ProjectTask;
import com.workpilot.entity.ressources.TeamMember;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class TasksTimeSheet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private ProjectTask task;  // Tâche concernée

    @ManyToOne
    private TeamMember teamMember; // Membre de l’équipe

    private LocalDate workDate;    // Jour travaillé

    private Double workedHours;    // Heures travaillées sur cette tâche ce jour-là
    private Double workedMD;       // Jours-homme (workedHours / 8h typiquement)

    // Getters / Setters
}

