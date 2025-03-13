package com.workpilot.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AffectationProject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double charge;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private double coutTotal;

    // Ressource affectée à un projet
    @ManyToOne
    @JoinColumn(name = "teamMember_id")
    private TeamMember teamMember;

    // Projet auquel la ressource est affectée
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    // L'équipe dans laquelle la ressource travaille sur ce projet
    @ManyToOne
    @JoinColumn(name = "team_id", nullable = true)
    private Team team;


    @PrePersist
    @PreUpdate
    public void calculerCoutTotal() {
        if (teamMember != null) {
            this.coutTotal = (teamMember.getCout() * charge) / 100;
        }
    }
}
