package com.workpilot.entity.Dashboard;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import com.workpilot.entity.ressources.Project;
import lombok.*;
import jakarta.persistence.*;
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlannedWorkload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String month;
    private int year;
    private double estimatedWorkloadPerResource;
    private int numberOfResources;
    private double totalEstimatedWorkload;

    @ManyToOne
    private Project project; // Projet associé
}