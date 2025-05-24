package com.workpilot.dto.PsrDTO;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PsrDTO {
    private Long id;
    private String reportTitle;
    private String comments;
    private String overallStatus;
    private String reference;
    private String edition;
    private LocalDate date;
    private String preparedBy;
    private String approvedBy;
    private String validatedBy;
    private LocalDate PreparedByDate;
    private LocalDate approvedByDate;
    private LocalDate validatedByDate;
    private String projectName;
    private String clientName;

    private String authorName;
    private Long projectId;
    private String status;
    private List<RisksDTO> risks;
    private List<DeliveriesDTO> deliveries;
    private List<TeamOrganizationDTO> teamOrganizations;
    private List<TaskTrackerDTO> taskTrackers;

    @NotNull(message = "La semaine est obligatoire")
    private String week;

    @NotNull(message = "L'année du rapport est obligatoire")
    private Integer reportYear;

    @NotNull(message = "La date du rapport est obligatoire")
    private LocalDate reportDate;
}
