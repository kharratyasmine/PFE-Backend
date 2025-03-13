package com.workpilot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Setter

public class TeamMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String image;
    private String name;
    private String initial;
    private Double allocation;
    private String  TeamRole;
    @JsonFormat(without = JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    private String holiday;
    private LocalDate dateEmbauche; // Date d'embauche utilisée pour calculer la séniorité
    @Enumerated(EnumType.STRING)
    private Seniorite seniorite; // Niveau de séniorité de la ressource
    private double cout; // Coût de la ressource calculé selon sa séniorité
    private String Note ;

    //   Plusieurs Membere peuvent appartenir à une seule `Team`
   @ManyToOne
    @JoinColumn(name = "team_id", nullable = true)
    @JsonIgnore // 🚀 Empêche la sérialisation infinie
    private Team team;

    // Une ressource peut être affectée à plusieurs projets
    @OneToMany(mappedBy = "teamMember")
    private List<AffectationProject> affectations;

   // Un membre peut être assigné à plusieurs tâches
   @OneToMany(mappedBy = "teamMember", cascade = CascadeType.ALL, orphanRemoval = true)
   private List<ProjectTask> tasks;



    // Calcul automatique de la séniorité et du coût en fonction de l'ancienneté
    @PrePersist
    @PreUpdate
    public void calculerSenioriteEtCout() {
        long anneesExperience = ChronoUnit.YEARS.between(this.dateEmbauche, LocalDate.now());

        if (anneesExperience < 2) {
            this.seniorite = Seniorite.JUNIOR;
            this.cout = 120;
        } else if (anneesExperience < 6) {
            this.seniorite = Seniorite.INTERMEDIAIRE;
            this.cout = 150;
        } else if (anneesExperience < 12) {
            this.seniorite = Seniorite.Senior;
            this.cout = 250;
        } else {
            this.seniorite = Seniorite.SeniorManager;
            this.cout = 280;
        }
    }

}
