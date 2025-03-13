package com.workpilot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FinancialDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String position;
    private Integer workload;
    private BigDecimal dailyCost;
    private BigDecimal totalCost;

    @ManyToOne
    @JoinColumn(name = "devis_id")
    private Devis devis;
}
