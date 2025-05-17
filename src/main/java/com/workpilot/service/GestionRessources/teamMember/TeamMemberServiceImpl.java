package com.workpilot.service.GestionRessources.teamMember;

import com.workpilot.dto.GestionRessources.TeamMemberDTO;
import com.workpilot.entity.ressources.Seniority;
import com.workpilot.entity.ressources.Team;
import com.workpilot.entity.ressources.TeamMember;
import com.workpilot.entity.ressources.TeamMemberAllocation;
import com.workpilot.repository.ressources.TeamMemberAllocationRepository;
import com.workpilot.repository.ressources.TeamMemberRepository;
import com.workpilot.repository.ressources.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TeamMemberServiceImpl implements TeamMemberService {

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberAllocationRepository teamMemberAllocationRepository;

    @Override
    public List<TeamMemberDTO> getAllTeamMembers() {
        return teamMemberRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TeamMemberDTO getTeamMemberById(Long id) {
        return teamMemberRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    public TeamMemberDTO createTeamMember(TeamMemberDTO teamMemberDTO) {
        try {
            TeamMember teamMember = convertToEntity(teamMemberDTO);

            // 🔥 Suggérer un rôle automatiquement si aucun n'est précisé
            if (teamMember.getStartDate() != null && teamMemberDTO.getRole() == null) {
                double exp = getYearsFromStartDate(teamMember.getStartDate());
                teamMember.setRole(suggestRole(exp));
            }

            teamMember = teamMemberRepository.save(teamMember);
            return convertToDTO(teamMember);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la création du membre : " + e.getMessage());
        }
    }


    @Override
    public TeamMemberDTO updateTeamMember(Long id, TeamMemberDTO teamMemberDTO) {
        return teamMemberRepository.findById(id)
                .map(existingMember -> {
                    TeamMember updatedMember = convertToEntity(teamMemberDTO);
                    updatedMember.setId(existingMember.getId());

                    // 🔥 Suggérer un rôle si pas fourni et startDate connue
                    if (updatedMember.getStartDate() != null && teamMemberDTO.getRole() == null) {
                        double exp = getYearsFromStartDate(updatedMember.getStartDate());
                        updatedMember.setRole(suggestRole(exp));
                    }

                    updatedMember = teamMemberRepository.save(updatedMember);
                    return convertToDTO(updatedMember);
                })
                .orElse(null);
    }

    @Override
    public void deleteTeamMember(Long id) {
        // Supprimer les allocations du membre
        List<TeamMemberAllocation> allocations = teamMemberAllocationRepository.findByTeamMemberId(id);
        teamMemberAllocationRepository.deleteAll(allocations);

        // Supprimer le membre
        teamMemberRepository.deleteById(id);
    }

    @Override
    public TeamMemberDTO moveTeamMember(Long teamMemberId, Long newTeamId) {
        TeamMember member = teamMemberRepository.findById(teamMemberId)
                .orElseThrow(() -> new RuntimeException("Membre non trouvé"));
        Team newTeam = teamRepository.findById(newTeamId)
                .orElseThrow(() -> new RuntimeException("Nouvelle équipe non trouvée"));

        // Vérifier si le membre a des allocations dans des projets de l'ancienne équipe
        List<TeamMemberAllocation> allocations = teamMemberAllocationRepository.findByTeamMemberId(member.getId());
        if (!allocations.isEmpty()) {
            // Option 1 : Supprimer les allocations
            teamMemberAllocationRepository.deleteAll(allocations);

            // Option 2 : Transférer les allocations vers la nouvelle équipe (si les projets sont communs)
            for (TeamMemberAllocation allocation : allocations) {
                if (newTeam.getProjects().contains(allocation.getProject())) {
                    allocation.setTeamMember(member); // Pas besoin de changer le projet
                    teamMemberAllocationRepository.save(allocation);
                } else {
                    teamMemberAllocationRepository.delete(allocation);
                }
            }
        }

        teamMemberRepository.save(member);

        return convertToDTO(member);
    }

    private double getYearsFromStartDate(LocalDate startDate) {
        if (startDate == null) return 0;
        long days = ChronoUnit.DAYS.between(startDate, LocalDate.now());
        return Math.round((days / 365.25) * 10.0) / 10.0;
    }


    private TeamMemberDTO convertToDTO(TeamMember teamMember) {
        TeamMemberDTO dto = new TeamMemberDTO();
        dto.setId(teamMember.getId());
        dto.setName(teamMember.getName());
        dto.setInitial(teamMember.getInitial());
        dto.setJobTitle(teamMember.getJobTitle());
        dto.setHoliday(teamMember.getHoliday());
        dto.setRole(teamMember.getRole());
        dto.setCost(teamMember.getCost());
        dto.setNote(teamMember.getNote());
        dto.setImage(teamMember.getImage());
        dto.setExperienceRange(getExperienceRange(teamMember.getStartDate()));
        dto.setStartDate(teamMember.getStartDate());
        dto.setTeams(
                teamMember.getTeams().stream()
                        .map(Team::getId)
                        .collect(Collectors.toList())
        );

        return dto;
    }

    private TeamMember convertToEntity(TeamMemberDTO dto) {
        TeamMember teamMember = new TeamMember();
        teamMember.setId(dto.getId());
        teamMember.setName(dto.getName());
        teamMember.setInitial(dto.getInitial());
        teamMember.setJobTitle(dto.getJobTitle());
        teamMember.setHoliday(dto.getHoliday());
        teamMember.setRole(dto.getRole());
        teamMember.setCost(dto.getCost());
        teamMember.setNote(dto.getNote());
        teamMember.setImage(dto.getImage());
        teamMember.setStartDate(dto.getStartDate());
        teamMember.setExperienceRange(getExperienceRange(teamMember.getStartDate()));



        // Associer le membre à une équipe si l'ID de l'équipe est fourni
        if (dto.getTeams() != null) {
            Set<Team> teams = dto.getTeams().stream()
                    .map(id -> teamRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Équipe non trouvée")))
                    .collect(Collectors.toSet());
            teamMember.setTeams(teams);
        }


        return teamMember;
    }

    @Override
    public List<TeamMemberDTO> getTeamMembersByTeamId(Long teamId) {
        return teamMemberRepository.findByTeamId(teamId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeamMemberDTO> getMembersByProjectId(Long projectId) {
        return teamMemberRepository.findMembersByProjectId(projectId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public String getExperienceRange(LocalDate startDate) {
        if (startDate == null) return "Inconnu";

        LocalDate now = LocalDate.now();
        long years = ChronoUnit.YEARS.between(startDate, now);

        if (years == 0) return "0 - 1 Year";
        return years + " - " + (years + 1) + " Years";
    }

    private Seniority suggestRole(double experience) {
        if (experience <= 2) {
            return Seniority.JUNIOR;
        } else if (experience <= 6) {
            return Seniority.INTERMEDIAIRE;
        } else if (experience <= 12) {
            return Seniority.SENIOR;
        } else {
            return Seniority.SENIOR_MANAGER;
        }
    }



}


