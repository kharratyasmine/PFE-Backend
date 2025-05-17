package com.workpilot.dto.GestionRessources;

import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class TeamDTO {
    private Long id;
    private String name;
    private Set<Long> projectIds;  // Set of Project IDs
    private List<TeamMemberDTO> members; // ✅ ajout des membres

    // Constructor
    public TeamDTO(Long id, String name, Set<Long> projectIds, List<TeamMemberDTO> members) {
        this.id = id;
        this.name = name;
        this.projectIds = projectIds;
        this.members = members;
    }


    // Constructeur 3 paramètres (déjà présent)
    public TeamDTO(Long id, String name, Set<Long> projectIds) {
        this.id = id;
        this.name = name;
        this.projectIds = projectIds;
    }

}
