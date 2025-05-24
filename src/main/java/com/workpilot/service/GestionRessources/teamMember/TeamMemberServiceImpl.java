package com.workpilot.service.GestionRessources.teamMember;

import com.workpilot.dto.GestionRessources.TeamMemberDTO;
import com.workpilot.entity.ressources.*;
import com.workpilot.repository.ressources.TeamMemberAllocationRepository;
import com.workpilot.repository.ressources.TeamMemberHistoryRepository;
import com.workpilot.repository.ressources.TeamMemberRepository;
import com.workpilot.repository.ressources.TeamRepository;
import com.workpilot.service.GestionRessources.TeamMemberHistory.TeamMemberHistoryService;
import com.workpilot.service.GestionRessources.TeamMemberHistory.TeamMemberHistoryServiceImpl;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
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

    @Autowired
    private TeamMemberHistoryService historyService;

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
            // ✅ VALIDATIONS
            if (teamMember.getName() == null || teamMember.getName().trim().isEmpty() || teamMember.getName().equalsIgnoreCase("Inconnu")) {
                throw new IllegalArgumentException("Le nom du membre est invalide.");
            }

            if (teamMember.getInitial() == null || teamMember.getInitial().trim().isEmpty()) {
                throw new IllegalArgumentException("Les initiales sont obligatoires.");
            }

            if (teamMember.getStartDate() == null) {
                throw new IllegalArgumentException("La date de début est obligatoire.");
            }

            // 🔥 Suggérer un rôle automatiquement si aucun n'est précisé
            if (teamMember.getStartDate() != null && teamMemberDTO.getRole() == null) {
                double exp = getYearsFromStartDate(teamMember.getStartDate());
                teamMember.setRole(suggestRole(exp));
            }
            teamMember.setStatus(calculateStatus(teamMember.getEndDate()));

            teamMember = teamMemberRepository.save(teamMember);
            return convertToDTO(teamMember);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la création du membre : " + e.getMessage());
        }
    }


    @Override
    @Transactional
    public TeamMemberDTO updateTeamMember(Long id, TeamMemberDTO teamMemberDTO) {
        return teamMemberRepository.findById(id)
                .map(existingMember -> {
                    TeamMember updatedMember = convertToEntity(teamMemberDTO);
                    updatedMember.setId(existingMember.getId());

                    // Sauvegarder l'historique pour chaque champ modifié
                    if (!existingMember.getName().equals(updatedMember.getName())) {
                        historyService.saveHistoryForField(id, "name", existingMember.getName(), updatedMember.getName(), "system");
                    }
                    if (!existingMember.getInitial().equals(updatedMember.getInitial())) {
                        historyService.saveHistoryForField(id, "initial", existingMember.getInitial(), updatedMember.getInitial(), "system");
                    }
                    if (!existingMember.getJobTitle().equals(updatedMember.getJobTitle())) {
                        historyService.saveHistoryForField(id, "jobTitle", existingMember.getJobTitle(), updatedMember.getJobTitle(), "system");
                    }
                    if (!existingMember.getRole().equals(updatedMember.getRole())) {
                        historyService.saveHistoryForField(id, "role", existingMember.getRole().toString(), updatedMember.getRole().toString(), "system");
                    }
                    if (!Objects.equals(existingMember.getCost(), updatedMember.getCost())) {
                        historyService.saveHistoryForField(id, "cost",
                                String.valueOf(existingMember.getCost()),
                                String.valueOf(updatedMember.getCost()),
                                "system");
                    }
                    if (!Objects.equals(existingMember.getStartDate(), updatedMember.getStartDate())) {
                        historyService.saveHistoryForField(id, "startDate",
                                existingMember.getStartDate() != null ? existingMember.getStartDate().toString() : "",
                                updatedMember.getStartDate() != null ? updatedMember.getStartDate().toString() : "",
                                "system");
                    }
                    if (!Objects.equals(existingMember.getEndDate(), updatedMember.getEndDate())) {
                        historyService.saveHistoryForField(id, "endDate",
                                existingMember.getEndDate() != null ? existingMember.getEndDate().toString() : "",
                                updatedMember.getEndDate() != null ? updatedMember.getEndDate().toString() : "",
                                "system");
                    }
                    if (!Objects.equals(existingMember.getNote(), updatedMember.getNote())) {
                        historyService.saveHistoryForField(id, "note",
                                existingMember.getNote() != null ? existingMember.getNote() : "",
                                updatedMember.getNote() != null ? updatedMember.getNote() : "",
                                "system");
                    }

                    // 🔥 Suggérer un rôle si pas fourni et startDate connue
                    if (updatedMember.getStartDate() != null && teamMemberDTO.getRole() == null) {
                        double exp = getYearsFromStartDate(updatedMember.getStartDate());
                        Seniority suggestedRole = suggestRole(exp);
                        if (!suggestedRole.equals(existingMember.getRole())) {
                            historyService.saveHistoryForField(
                                    id,
                                    "role",
                                    existingMember.getRole().toString(),
                                    suggestedRole.toString(),
                                    "system"
                            );
                        }
                        updatedMember.setRole(suggestedRole);
                    }

                    // Mettre à jour le statut
                    String newStatus = calculateStatus(updatedMember.getEndDate());
                    if (!newStatus.equals(existingMember.getStatus())) {
                        historyService.saveHistoryForField(
                                id,
                                "status",
                                existingMember.getStatus() != null ? existingMember.getStatus() : "",
                                newStatus,
                                "system"
                        );
                    }
                    updatedMember.setStatus(newStatus);

                    // Mettre à jour l'expérience
                    String newExperienceRange = getExperienceRange(updatedMember.getStartDate());
                    if (!newExperienceRange.equals(existingMember.getExperienceRange())) {
                        historyService.saveHistoryForField(
                                id,
                                "experienceRange",
                                existingMember.getExperienceRange() != null ? existingMember.getExperienceRange() : "",
                                newExperienceRange,
                                "system"
                        );
                    }
                    updatedMember.setExperienceRange(newExperienceRange);

                    // Sauvegarder les modifications
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
        dto.setEndDate(teamMember.getEndDate());
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
        teamMember.setEndDate(dto.getEndDate());
        teamMember.setStatus(calculateStatus(dto.getEndDate()));
        teamMember.setExperienceRange(getExperienceRange(teamMember.getStartDate()));

        // ✅ Gérer le champ fake
        boolean isFake = dto.isFake(); // assure-toi que ton DTO a bien un boolean getFake()
        teamMember.setFake(isFake);

        if (isFake) {
            // ⚠️ Un seul membre fake autorisé par rôle
            teamMemberRepository.findByRoleAndFake(String.valueOf(dto.getRole()), true).ifPresent(existing -> {
                if (dto.getId() == null || !existing.getId().equals(dto.getId())) {
                    throw new RuntimeException("Un membre fake existe déjà pour le rôle " + dto.getRole());
                }
            });
        }

        // ✅ Associer aux équipes si besoin
        if (dto.getTeams() != null) {
            Set<Team> teams = dto.getTeams().stream()
                    .map(id -> teamRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("Équipe non trouvée")))
                    .collect(Collectors.toSet());
            teamMember.setTeams(teams);
        }

        return teamMember;
    }


    private String calculateStatus(LocalDate endDate) {
        if (endDate == null || endDate.isAfter(LocalDate.now())) {
            return "En poste"; // ✅ Encore en activité
        } else {
            return "Inactif"; // ❌ Fin de contrat dépassée
        }
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


