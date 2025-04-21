package com.workpilot.dto;

import com.workpilot.entity.ressources.TeamMember;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@Builder
@ToString
public class DemandeDTO {
    private Long id;
    private String name;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Long projectId; // Référence au projet
    private String projectName;
    private Set<Long> teamMemberIds; // Liste des IDs des membres de l'équipe
    private String scope;
    private String requirements;
    private Long generatedTeamId;
    private Long generatedDevisId;

    public DemandeDTO(Long id, String name, LocalDate dateDebut, LocalDate dateFin,
                       Long projectId,String projectName, Set<Long> teamMemberIds,String scope, String requirements ,Long generatedTeamId,Long generatedDevisId) {
        this.id = id;
        this.name = name;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.projectId = projectId;
        this.projectName = projectName;
        this.teamMemberIds = teamMemberIds;
        this.scope = scope;
        this.requirements = requirements;
        this.generatedTeamId = generatedTeamId;
        this.generatedDevisId = generatedDevisId;
    }



}
