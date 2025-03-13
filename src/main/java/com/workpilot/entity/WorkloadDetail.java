package com.workpilot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class WorkloadDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String period;
    private Integer estimatedWorkload;
    private Integer publicHolidays;
    private Integer actualWorkload;

    @ManyToOne
    private Devis devis;
}
