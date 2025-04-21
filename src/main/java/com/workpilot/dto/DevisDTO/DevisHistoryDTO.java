package com.workpilot.dto.DevisDTO;

import lombok.Data;
import lombok.*;
import java.time.LocalDate;

@Data
@Builder
public class DevisHistoryDTO {
    private Long id;
    private String version;
    private String modificationDescription;
    private String action;
    private LocalDate date;
    private String name;
    private Long devisId;
}
