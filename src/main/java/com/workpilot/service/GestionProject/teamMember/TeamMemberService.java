package com.workpilot.service.GestionProject.teamMember;

import com.workpilot.dto.TeamMemberDTO;
import com.workpilot.entity.ressources.TeamMember;

import java.util.List;

public interface TeamMemberService {

    // Récupérer tous les membres d'équipe
    List<TeamMemberDTO> getAllTeamMembers();

    // Récupérer un membre par son ID
    TeamMemberDTO getTeamMemberById(Long id);

    // Créer un nouveau membre
    TeamMemberDTO createTeamMember(TeamMemberDTO teamMemberDTO);

    // Mettre à jour un membre existant
    TeamMemberDTO updateTeamMember(Long id, TeamMemberDTO teamMemberDTO);

    // Supprimer un membre
    void deleteTeamMember(Long id);

    // Déplacer un membre d'une équipe à une autre
    TeamMemberDTO moveTeamMember(Long teamMemberId, Long newTeamId);

    List<TeamMemberDTO> getMembersByProjectId(Long projectId);

    List<TeamMemberDTO> getTeamMembersByTeamId(Long teamId);
}