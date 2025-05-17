package com.workpilot.entity.ressources;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.workpilot.entity.devis.Devis;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "demandes")

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class Demande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column(nullable = false)
    private LocalDate dateFin;

    @Column(length = 500)
    private String scope;

    @Column(length = 1000)
    private String requirements;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnore
    private Project project;

    @ManyToMany(fetch = FetchType.LAZY)
    private List<TeamMember> teamMembers;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "generated_team_id")
    private Team generatedTeam;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "generated_devis_id")
    private Devis generatedDevis;


    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "demande_id")
    private List<FakeMember> fakeMembers = new ArrayList<>();


}
