package com.workpilot.service.GestionProject.allocation;

import com.workpilot.dto.TeamMemberAllocationDTO;

import java.util.List;
import java.util.Optional;

public interface TeamMemberAllocationService {

    // Ajouter une allocation pour un membre dans un projet
    TeamMemberAllocationDTO addAllocation(TeamMemberAllocationDTO allocationDTO);

    // Mettre à jour une allocation existante
    TeamMemberAllocationDTO updateAllocation(Long id, TeamMemberAllocationDTO allocationDTO);

    // Supprimer une allocation
    void deleteAllocation(Long id);

    // Récupérer une allocation par membre et projet
    TeamMemberAllocationDTO getAllocationByMemberAndProject(Long memberId, Long projectId);

    List<TeamMemberAllocationDTO> getAllocationsByMember(Long memberId);

    Optional<TeamMemberAllocationDTO> findByTeamMemberAndProject(Long memberId, Long projectId);


}