package com.workpilot.entity.devis;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProposalSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
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

    @ManyToOne
    @JoinColumn(name = "devis_id", nullable = false)
    @JsonBackReference
    private Devis devis;
}
