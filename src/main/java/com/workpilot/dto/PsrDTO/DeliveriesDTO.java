package com.workpilot.dto.PsrDTO;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveriesDTO {
    private Long id;
    private String deliveriesName;
    private String description;
    private LocalDate plannedDate;
    private LocalDate effectiveDate;
    private String version ;
    private String status; // Delivered, Pending, Late
    private String deliverySupport ;
    private String customerFeedback;
    private String week;
    private Integer reportYear;
}
