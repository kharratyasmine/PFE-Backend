package com.workpilot.entity.PSR;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class Risks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    private String probability; // High, Medium, Low
    private String impact; // High, Medium, Low
    private String mitigationPlan;

    @ManyToOne
    private Psr psr;

}
