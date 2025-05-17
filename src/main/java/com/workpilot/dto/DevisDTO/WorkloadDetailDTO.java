package com.workpilot.dto.DevisDTO;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkloadDetailDTO {
    private Long id;
    private String period;
    private Integer estimatedWorkload;
    private Integer publicHolidays;
    private List<LocalDate> publicHolidayDates;
    private Integer numberOfResources;
    private Integer totalEstimatedWorkload;
    private Integer totalWorkload;
    private String note;
    private Long devisId;
}
