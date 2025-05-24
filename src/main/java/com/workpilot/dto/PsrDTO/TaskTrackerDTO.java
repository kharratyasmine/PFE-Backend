package com.workpilot.dto.PsrDTO;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskTrackerDTO {
    private Long id;
    private Long psrId;
    private Long projectId;
    private String description;

    private String who;

    private LocalDate startDate;
    private LocalDate estimatedEndDate;
    private LocalDate effectiveEndDate;

    private Double workedMD;
    private Double estimatedMD;
    private Double remainingMD;

    private Integer progress;

    private String currentStatus;
    private Double effortVariance;
    private String deviationReason;
    private String note;

    private String week;
    private Integer reportYear;
}
