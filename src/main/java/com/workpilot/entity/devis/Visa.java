package com.workpilot.entity.devis;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "visa", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"action", "devis_id"})
})
public class Visa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;
    private String name;
    private LocalDate date;
    private String visa;

    @ManyToOne
    @JoinColumn(name = "devis_id", nullable = false)
    private Devis devis;
}
