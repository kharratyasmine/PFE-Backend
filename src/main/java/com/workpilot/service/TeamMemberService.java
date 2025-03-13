package com.workpilot.service;

import com.workpilot.entity.TeamMember;

import java.util.List;

public interface TeamMemberService {
   List<TeamMember> getAllTeamMembers();
    TeamMember getTeamMemberById(Long id);
    TeamMember saveTeamMember(TeamMember teamMember);
    TeamMember updateTeamMember(Long id, TeamMember teamMember);
    void deleteTeamMember(Long id);
    List<TeamMember> getMembersByTeam(Long id);


    // 🔹 Ajoute cette ligne si elle manque
    TeamMember addMemberToTeam(Long userId, Long teamId, String role, double allocation);
}
