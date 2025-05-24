package com.workpilot.dto.PsrDTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RisksDTO {

    private Long id;
    private String description;
    private String origin;
    private String category;

    private String openDate;
    private String dueDate;

    private String causes;
    private String consequences;
    private String appliedMeasures;

    private String probability;
    private String gravity;
    private String criticality;
    private String measure;
    private String riskAssessment;
    private String riskTreatmentDecision;
    private String justification;

    private String idAction;
    private String riskStat;

    private String closeDate;

    private String impact;
    private String mitigationPlan;

    private Long psrId;

    private String week;
    private Integer reportYear;
}
