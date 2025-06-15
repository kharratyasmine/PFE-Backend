package com.workpilot.dto.PsrDTO;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyReportDTO {
    private Long id;
    private String month;
    private int weekNumber;
    private int year;
    private String projectName;
    private Double workingDays;
    private Double estimatedDays;
    private Double effortVariance;
    private Long psrId;
    private String week;
    private Map<String, Double> effortVarianceByWeek = new HashMap<>();

}
