package com.workpilot.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

  //  Une équipe peut avoir plusieurs membres (Relation OneToMany avec `TeamMember`)
  @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnoreProperties("team")  // 🔥 Évite la boucle infinie avec TeamMember
  private List<TeamMember> members = new ArrayList<>();

    // Chaque équipe appartient à un projet
    @ManyToOne
    @JoinColumn(name = "project_id")
    @JsonIgnoreProperties("teams")
    private Project project;
}
