package com.workpilot.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssignmentDTO {
    private Long id;
    private Long teamMemberId;
    private String teamMemberName;
    private int progress;
    private double workedMD;
    private double estimatedMD;
    private double remainingMD;

    private LocalDate estimatedStartDate;
    private LocalDate estimatedEndDate;
    private LocalDate effectiveStartDate;
    private LocalDate effectiveEndDate;
}
