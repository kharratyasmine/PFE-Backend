package com.workpilot.dto.GestionRessources;

import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlannedWorkloadMemberDTO {
    private Long id;
    private int month;
    private int year;
    private double workload; // charge planifiée pour ce mois
    private String note;
    private Long teamMemberId;
    private String teamMemberName;
    private String teamMemberRole;
    private Long projectId;
    private String projectName;

}
