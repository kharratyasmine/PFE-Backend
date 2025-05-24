package com.workpilot.dto.PsrDTO;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamOrganizationDTO {
    private Long id;
    private String fullName;
    private String initial;
    private String role;
    private String project;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private String Allocation;
    private String ComingFromTeam;
    private String GoingToTeam;
    private String Holiday ;
    private String teamName; // facultatif
    private Long PsrId;

    private String week;
    private Integer reportYear;
}
