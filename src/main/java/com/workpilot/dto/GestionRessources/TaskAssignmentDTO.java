package com.workpilot.dto.GestionRessources;

import lombok.*;
import java.time.LocalDate;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssignmentDTO {
    private Long id;
    private Long teamMemberId;
    private String teamMemberName;
    private int progress;
    private Double workedMD;
    private Double estimatedMD;
    private Double remainingMD;

    private List<LocalDate> workedDaysList;
    private List<LocalDate> leaveDaysList; // jours marqués comme Leave dans le calendrier

    private LocalDate estimatedStartDate;
    private LocalDate estimatedEndDate;
    private LocalDate effectiveStartDate;
    private LocalDate effectiveEndDate;
}