package com.workpilot.dto.PsrDTO;

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
    private LocalDate reportDate;
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
    private String week;
    private String authorName;
    private Long projectId;
    private String status;
    private List<RisksDTO> risks;
    private List<DeliveriesDTO> deliveries;
    private List<TeamOrganizationDTO> teamOrganizations;
}
