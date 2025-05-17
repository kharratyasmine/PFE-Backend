package com.workpilot.dto.GestionRessources;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

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
    private List<FakeMemberDTO> fakeMembers;

    public DemandeDTO(Long id, String name, LocalDate dateDebut, LocalDate dateFin,
                      Long projectId, String projectName, Set<Long> teamMemberIds,
                      String scope, String requirements,
                      Long generatedTeamId, Long generatedDevisId,
                      List<FakeMemberDTO> fakeMembers) {
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
        this.fakeMembers = fakeMembers;
    }


}
