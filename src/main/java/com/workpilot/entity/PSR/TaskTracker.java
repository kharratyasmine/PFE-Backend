package com.workpilot.entity.PSR;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "task_tracker")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskTracker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private String who;

    private LocalDate startDate;
    private LocalDate estimatedEndDate;
    private LocalDate effectiveEndDate;

    private Double workedMD;
    private Double estimatedMD;
    private Double remainingMD;

    private Integer progress;

    private String currentStatus;
    private Double effortVariance;
    private String deviationReason;
    private String note;                 // Remarques supplémentaires

    @Column(name = "week")
    private String week;

    @Column(name = "report_year")
    private Integer reportYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "psr_id")
    private Psr psr;
}
