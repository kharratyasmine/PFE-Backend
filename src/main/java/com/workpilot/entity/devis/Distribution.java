package com.workpilot.entity.devis;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Distribution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String function;
    private boolean partial;
    private boolean complete;
    private String type;

    @ManyToOne
    @JoinColumn(name = "devis_id")
    @JsonBackReference
    private Devis devis;

}