package com.workpilot.service.GestionRessources.team;

import com.workpilot.dto.GestionRessources.TeamDTO;
import com.workpilot.dto.GestionRessources.TeamMemberDTO;

import java.util.List;

public interface TeamService {

  // Récupérer toutes les équipes
  List<TeamDTO> getAllTeams();

  // Récupérer une équipe par son ID
  TeamDTO getTeamById(Long id);

  // Créer une nouvelle équipe
  TeamDTO createTeam(TeamDTO teamDTO);

  // Mettre à jour une équipe existante
  TeamDTO updateTeam(Long id, TeamDTO teamDTO);

  // Supprimer une équipe
  void deleteTeam(Long id);

  // Ajouter un membre à une équipe
  TeamDTO addMemberToTeam(Long teamId, Long memberId);

  // Associer une équipe à un projet
  TeamDTO addProjectToTeam(Long teamId, Long projectId);
  // Supprimer un membre d'une équipe
  void removeMemberFromTeam(Long teamId, Long memberId);

  // Récupérer les membres d'une équipe
  List<TeamMemberDTO> getMembersOfTeam(Long teamId);

  List<TeamMemberDTO> getAvailableMembers(Long teamId);

}