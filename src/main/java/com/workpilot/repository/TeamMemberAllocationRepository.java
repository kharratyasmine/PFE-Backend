package com.workpilot.repository;

import com.workpilot.entity.ressources.Project;
import com.workpilot.entity.ressources.TeamMember;
import com.workpilot.entity.ressources.TeamMemberAllocation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberAllocationRepository extends JpaRepository<TeamMemberAllocation, Long> {

    // Récupérer toutes les allocations d'un membre par son ID
    List<TeamMemberAllocation> findByTeamMemberId(Long teamMemberId);

    // Récupérer une allocation spécifique par membre et projet
    // Dans TeamMemberAllocationRepository
    List<TeamMemberAllocation> findAllByTeamMemberIdAndProjectId(Long teamMemberId, Long projectId);

}
