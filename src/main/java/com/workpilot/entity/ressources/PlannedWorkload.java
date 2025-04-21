package com.workpilot.entity.ressources;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
    @JoinColumn(name = "project_id")
    private Project project;
}
