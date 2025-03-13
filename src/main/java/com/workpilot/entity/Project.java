package com.workpilot.entity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String projectType;
    private String description;
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate endDate;

    private String scope;
    private String requirements;

    @Enumerated(EnumType.STRING)  // ✅ Indique à Hibernate d'enregistrer la valeur sous forme de chaîne
    private Status status;

    @OneToMany(mappedBy = "project")
    private List<Devis> devisList;

    // 🔹 Plusieurs projets peuvent être créés par un seul utilisateur (chef de projet)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    @JsonIgnoreProperties("projects")
    private User user;

    // Relation : Un projet appartient à un seul client
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = true)
    @JsonIgnoreProperties("projects")
    private Client client;  // Assurez-vous que cette relation est bien définie


    // Un projet peut avoir plusieurs ressources affectées avec un pourcentage de charge
    @OneToMany(mappedBy = "project")
    private List<AffectationProject> affectations;

    // Un projet peut contenir plusieurs équipes
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("project")  // 🔥 Évite la boucle infinie !
    private List<Team> teams = new ArrayList<>();


    // Un projet contient plusieurs tâches
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<ProjectTask> tasks;
}

