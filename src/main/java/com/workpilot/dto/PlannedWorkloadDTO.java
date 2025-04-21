package com.workpilot.dto;

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
    private boolean manualTotal;
    private double total;
    private Long projectId;
    private String projectName;
}
