package com.workpilot.dto.DevisDTO;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoicingDetailDTO {
    private Long id;
    private String description;
    private LocalDate invoicingDate;
    private BigDecimal amount;
    private Long devisId;
}
