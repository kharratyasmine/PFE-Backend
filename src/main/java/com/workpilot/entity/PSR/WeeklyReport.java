package com.workpilot.entity.PSR;

import com.workpilot.entity.PSR.Psr;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String month;
    private int weekNumber;
    private int year;
    private String week;
    private String projectName;
    private Double workingDays;
    private Double estimatedDays;
    private Double effortVariance;

    @ManyToOne
    private Psr psr;
}