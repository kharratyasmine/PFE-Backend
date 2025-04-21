package com.workpilot.service.GestionProject.team;

import com.workpilot.dto.TeamDTO;
import com.workpilot.dto.TeamMemberDTO;
import com.workpilot.entity.ressources.Team;
import com.workpilot.entity.ressources.TeamMember;
import com.workpilot.entity.ressources.Project;
import com.workpilot.entity.ressources.TeamMemberAllocation;
import com.workpilot.repository.TeamMemberAllocationRepository;
import com.workpilot.repository.TeamRepository;
import com.workpilot.repository.TeamMemberRepository;
import com.workpilot.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TeamServiceImpl implements TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TeamMemberAllocationRepository teamMemberAllocationRepository;

    @Override
    public List<TeamDTO> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(team -> convertToDTO(team, null))
                .collect(Collectors.toList());
    }

    @Override
    public TeamDTO getTeamById(Long id) {
        return teamRepository.findById(id)
                .map(team -> convertToDTO(team, null))
                .orElse(null);
    }

    @Override
    public TeamDTO createTeam(TeamDTO teamDTO) {
        Team team = convertToEntity(teamDTO);
        team = teamRepository.save(team);
        return convertToDTO(team, null);
    }

    @Override
    public TeamDTO updateTeam(Long id, TeamDTO teamDTO) {
        return teamRepository.findById(id)
                .map(existingTeam -> {
                    existingTeam.setName(teamDTO.getName());

                    if (teamDTO.getProjectIds() != null) {
                        Set<Project> projects = teamDTO.getProjectIds().stream()
                                .map(projectId -> projectRepository.findById(projectId)
                                        .orElseThrow(() -> new EntityNotFoundException("Projet introuvable : " + projectId)))
                                .collect(Collectors.toSet());
                        existingTeam.setProjects(projects);
                    }

                    Team updatedTeam = teamRepository.save(existingTeam);
                    return convertToDTO(updatedTeam, null);
                })
                .orElseThrow(() -> new EntityNotFoundException("Équipe non trouvée : " + id));
    }

    @Override
    public void deleteTeam(Long id) {
        // Récupérer l'équipe (ou lancer une exception si inexistante)
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Équipe non trouvée avec l'ID : " + id));

        // Pour chaque membre de l'équipe, supprimer toutes leurs allocations
        if (team.getMembers() != null) {
            for (TeamMember member : team.getMembers()) {
                List<TeamMemberAllocation> allocations = teamMemberAllocationRepository.findByTeamMemberId(member.getId());
                allocations.forEach(allocation -> teamMemberAllocationRepository.delete(allocation));
                // Supprimer l'équipe du côté du membre (si relation bidirectionnelle)
                member.getTeams().remove(team);
                teamMemberRepository.save(member);
            }
        }

        // Dissocier l'équipe de ses projets
        if (team.getProjects() != null) {
            for (Project project : team.getProjects()) {
                project.getTeams().remove(team);
                projectRepository.save(project);
            }
        }

        // Supprimer l'équipe de la base
        teamRepository.delete(team);
    }





    @Override
    public TeamDTO addMemberToTeam(Long teamId, Long memberId) {
        Team team = teamRepository.findById(teamId).orElse(null);
        TeamMember member = teamMemberRepository.findById(memberId).orElse(null);

        if (team != null && member != null) {
            member.getTeams().add(team);
            team.getMembers().add(member);
            teamMemberRepository.save(member);
            teamRepository.save(team);
            return convertToDTO(team, null);
        }
        return null;
    }

    public void removeMemberFromTeam(Long teamId, Long memberId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Équipe non trouvée"));
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Membre non trouvé"));

        // Supprimer le membre de l'équipe
        team.getMembers().remove(member);
        member.getTeams().remove(team); // <-- Très important pour MAJ des 2 côtés

        teamRepository.save(team); // Sauvegarder les modifications
        teamMemberRepository.save(member); // Sauvegarder aussi le membre
    }



    @Override
    public List<TeamMemberDTO> getMembersOfTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Équipe introuvable avec l'id : " + teamId));

        return team.getMembers().stream()
                .map(member -> convertToMemberDTO(member, null)) // Sans contexte projet
                .collect(Collectors.toList());
    }



    @Override
    public TeamDTO addProjectToTeam(Long teamId, Long projectId) {
        Team team = teamRepository.findById(teamId).orElse(null);
        Project project = projectRepository.findById(projectId).orElse(null);

        if (team != null && project != null) {
            team.getProjects().add(project);
            project.getTeams().add(team);
            projectRepository.save(project);
            teamRepository.save(team);
            return convertToDTO(team, projectId);
        }
        return null;
    }

    private TeamDTO convertToDTO(Team team, Long projectId) {
        TeamDTO dto = new TeamDTO();
        dto.setId(team.getId());
        dto.setName(team.getName());

        if (team.getProjects() != null) {
            dto.setProjectIds(
                    team.getProjects().stream()
                            .map(Project::getId)
                            .collect(Collectors.toSet())
            );
        }

        if (team.getMembers() != null) {
            dto.setMembers(
                    team.getMembers().stream()
                            .map(member -> convertToMemberDTO(member, projectId))
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }


    private TeamMemberDTO convertToMemberDTO(TeamMember member, Long projectId) {
        Double allocation = teamMemberAllocationRepository
                .findAllByTeamMemberIdAndProjectId(member.getId(), projectId)
                .stream()
                .mapToDouble(TeamMemberAllocation::getAllocation)
                .sum();


        return new TeamMemberDTO(
                member.getId(),
                member.getName(),
                member.getInitial(),
                member.getJobTitle(),
                member.getHoliday(),
                member.getRole(),
                member.getCost(),
                member.getNote(),
                member.getImage(),
                member.getTeams().stream().map(Team::getId).collect(Collectors.toList()),
                member.getExperienceRange(),
                allocation
        );
    }

    private Team convertToEntity(TeamDTO dto) {
        Team team = new Team();
        team.setId(dto.getId());
        team.setName(dto.getName());
        return team;
    }

    @Override
    public List<TeamMemberDTO> getAvailableMembers(Long teamId) {
        try {
            Team team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new RuntimeException("Team not found"));

            List<TeamMember> allMembers = teamMemberRepository.findAll();
            List<TeamMember> assignedMembers = new ArrayList<>(team.getMembers());

            return allMembers.stream()
                    .filter(member -> !assignedMembers.contains(member))
                    .map(member -> convertToMemberDTO(member, null))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace(); // 🔥 imprime l'erreur complète
            throw new RuntimeException("Erreur dans getAvailableMembers : " + e.getMessage());
        }
    }




}
