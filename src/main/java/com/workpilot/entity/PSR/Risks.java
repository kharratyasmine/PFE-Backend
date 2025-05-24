package com.workpilot.entity.PSR;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Risks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String description;
    private String origin;
    private String category;

    private LocalDate openDate;
    private LocalDate dueDate;

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

    private LocalDate closeDate;

    private String impact;
    private String mitigationPlan;

    @Column(name = "week")
    private String week;

    @Column(name = "report_year")
    private Integer reportYear;
    @ManyToOne
    private Psr psr;
}
