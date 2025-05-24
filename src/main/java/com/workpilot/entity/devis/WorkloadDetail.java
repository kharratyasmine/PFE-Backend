package com.workpilot.entity.devis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.workpilot.entity.ressources.Demande;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "WorkloadDetail")
@Builder
public class WorkloadDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String period;
    private Integer estimatedWorkload;
    @ElementCollection
    private List<LocalDate> publicHolidayDates;
    private Integer publicHolidays;
    private Integer numberOfResources;
    private Integer totalEstimatedWorkload;
    private Integer totalWorkload;
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devis_id", nullable = false)
    private Devis devis;


    @ManyToOne
    @JoinColumn(name = "demande_id", nullable = false)
    @JsonIgnore
    private Demande demande;


}
