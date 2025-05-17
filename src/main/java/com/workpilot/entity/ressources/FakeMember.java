package com.workpilot.entity.ressources;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FakeMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;        // Ex: "Inconnu (Junior)"
    private String role;        // JUNIOR, SENIOR, EXPERT (texte ou Enum)
    private String initial;     // Ex: "IJ" → généré automatiquement ou fourni
    private String note;        // Champ libre : "Ajouté automatiquement", etc.
}
