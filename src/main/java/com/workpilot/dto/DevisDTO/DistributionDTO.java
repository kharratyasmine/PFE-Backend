package com.workpilot.dto.DevisDTO;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributionDTO {
    private Long id;
    private String name;
    private String function;
    private Boolean partial;
    private Boolean complete;
    private Long devisId;
    private String type;

}
