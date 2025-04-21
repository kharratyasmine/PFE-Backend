package com.workpilot.entity.ressources;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class TaskAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProjectTask task;

    @ManyToOne(fetch = FetchType.LAZY)
    private TeamMember teamMember;

    private int progress;
    private double workedMD;
    private double estimatedMD;
    private double remainingMD;

    private LocalDate estimatedStartDate;
    private LocalDate estimatedEndDate;
    private LocalDate effectiveStartDate;
    private LocalDate effectiveEndDate;
}