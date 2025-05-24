package com.workpilot.dto.PsrDTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyReportDTO {
    private Long id;
    private Long psrId;
    private String projectName;
    private Double workingDays;
    private Double estimatedDays;
    private Double effortVariance;
    private String week;
    private Integer reportYear;
}
