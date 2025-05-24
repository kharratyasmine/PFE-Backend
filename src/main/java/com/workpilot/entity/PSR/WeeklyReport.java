package com.workpilot.entity.PSR;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "weekly_reports")
@Getter
@Setter
public class WeeklyReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "psr_id")
    private Psr psr;

    private String projectName;
    private Double workingDays;
    private Double estimatedDays;
    private Double effortVariance;
    @Column(name = "week")
    private String week;

    @Column(name = "report_year")
    private Integer reportYear;


}


