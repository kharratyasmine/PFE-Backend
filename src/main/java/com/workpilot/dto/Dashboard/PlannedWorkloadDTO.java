package com.workpilot.dto.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlannedWorkloadDTO {
    private Long id;
    private String month;
    private int year;
    private double estimatedWorkloadPerResource;
    private int numberOfResources;
    private double totalEstimatedWorkload;
    private Long projectId;
    private String projectName;
}

