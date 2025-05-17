package com.workpilot.dto.DevisDTO;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProposalSummaryDTO {
    private String customer;
    private String project;
    private String projectType;
    private String proposalValidity;
    private int estimatedWorkload;
    private LocalDate possibleStartDate;
    private LocalDate estimatedEndDate;
    private String technicalAspect;
    private String organizationalAspect;
    private String commercialAspect;
    private String qualityAspect;
    private Long devisId;
}
