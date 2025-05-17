package com.workpilot.entity.devis;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.workpilot.entity.ressources.Demande;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import java.math.BigDecimal;

@Entity
@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "FinancialDetail")
public class FinancialDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String position;
    private Integer workload;
    private BigDecimal dailyCost;
    private BigDecimal totalCost;

    @ManyToOne
    @JoinColumn(name = "demande_id", nullable = false)
    @JsonIgnore
    private Demande demande;


    @ManyToOne
    @JoinColumn(name = "devis_id", nullable = false)
    @JsonIgnore
    private Devis devis;
}
