package com.workpilot.entity.ressources;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlannedWorkloadMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ✅ Identifiant obligatoire

    private String month;
    private int year;
    private int workload;
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    private TeamMember teamMember;

    @ManyToOne(fetch = FetchType.LAZY)
    private Project project;
}
