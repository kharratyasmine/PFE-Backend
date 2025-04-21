package com.workpilot.dto.DevisDTO;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialDetailDTO {
    private Long id;
    private String position;
    private Integer workload;
    private BigDecimal dailyCost;
    private BigDecimal totalCost;
}
