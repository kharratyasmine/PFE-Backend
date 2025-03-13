package com.workpilot.service;

import com.workpilot.entity.Team;
import com.workpilot.entity.TeamMember;
import jakarta.validation.Valid;

import java.util.List;

public interface TeamService {
  List<Team> getAllTeam();
    Team getTeamById(Long id);
    Team saveTeam(Team team);
    Team updateTeam(Long id, Team newTeam);
    void deleteTeam(Long id);
    void assignerTeamMemberTeam(Long teamMemberId, Long teamId);
    /*    List<TeamMember> getAllMembers();
    List<TeamMember> getMembersByTeam(Long teamId);*/

}
