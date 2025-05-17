package com.workpilot.dto.PsrDTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RisksDTO {
    private Long id;
    private String description;
    private String probability;
    private String impact;
    private String mitigationPlan;
}
