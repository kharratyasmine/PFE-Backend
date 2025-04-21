package com.workpilot.dto.DevisDTO;

import lombok.*;

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
    private Long devisId;
}
