package com.workpilot.entity.devis;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "WorkloadDetail")
@Builder
public class WorkloadDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String period;
    private Integer estimatedWorkload;
    private Integer publicHolidays;



    @ManyToOne
    @JoinColumn(name = "devis_id", nullable = false)
    private Devis devis;
}
