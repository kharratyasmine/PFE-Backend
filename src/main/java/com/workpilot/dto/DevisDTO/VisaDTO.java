package com.workpilot.dto.DevisDTO;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisaDTO {
    private Long id;
    private String action;
    private String name;
    private LocalDate date;
    private String visa;
    private Long devisId;
}

