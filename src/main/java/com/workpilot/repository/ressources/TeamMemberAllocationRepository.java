package com.workpilot.repository.ressources;

import com.workpilot.entity.ressources.TeamMemberAllocation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamMemberAllocationRepository extends JpaRepository<TeamMemberAllocation, Long> {

    // Récupérer toutes les allocations d'un membre par son ID
    List<TeamMemberAllocation> findByTeamMemberId(Long teamMemberId);
    List<TeamMemberAllocation> findAllByTeamMemberIdAndProjectId(Long teamMemberId, Long projectId);

    List<TeamMemberAllocation> findAllByTeamMemberIdAndProjectIdAndTeamId(Long memberId, Long projectId, Long teamId);
    List<TeamMemberAllocation> findAllByTeamMemberIdAndTeamId(Long memberId, Long teamId);
    List<TeamMemberAllocation> findAllByTeamId(Long teamId);
    List<TeamMemberAllocation> findAllByProjectId(Long projectId);
    List<TeamMemberAllocation> findAllByProjectIdAndTeamMemberId(Long projectId, Long teamMemberId);

}
