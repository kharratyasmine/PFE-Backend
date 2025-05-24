package com.workpilot.entity.PSR;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class TeamOrganization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String role;
    private String initial;
    private String project;
    private LocalDate PlannedStartDate;
    private LocalDate PlannedEndDate;
    private String Allocation;
    private String ComingFromTeam;
    private String GoingToTeam;
    private String Holiday ;
    private String teamName;

    @Column(name = "week")
    private String week;

    @Column(name = "report_year")
    private Integer reportYear;

    @ManyToOne
    private Psr psr;

}
