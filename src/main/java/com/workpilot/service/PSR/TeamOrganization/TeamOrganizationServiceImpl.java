package com.workpilot.service.PSR.TeamOrganization;

import com.workpilot.dto.PsrDTO.TeamOrganizationDTO;
import com.workpilot.entity.PSR.Psr;
import com.workpilot.entity.PSR.TeamOrganization;
import com.workpilot.entity.ressources.*;
import com.workpilot.repository.Psr.PsrRepository;
import com.workpilot.repository.Psr.TeamOrganizationRepository;
import com.workpilot.repository.ressources.TeamMemberAllocationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamOrganizationServiceImpl implements TeamOrganizationService {

    @Autowired
    private TeamOrganizationRepository teamOrganizationRepository;

    @Autowired
    private PsrRepository psrRepository;

    @Autowired
    private TeamMemberAllocationRepository allocationRepository;

    /**
     * Retourne toutes les organisations d’équipe (sans filtrage).
     */
    @Override
    public List<TeamOrganizationDTO> getAll() {
        return teamOrganizationRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retourne toutes les équipes associées à un PSR spécifique.
     */
    @Override
    public List<TeamOrganizationDTO> getTeamByPsrId(Long psrId) {
        return teamOrganizationRepository.findByPsrId(psrId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crée une nouvelle organisation d’équipe pour un PSR donné.
     */
    @Override
    public TeamOrganizationDTO createTeamOrganization(Long psrId, TeamOrganizationDTO dto) {
        Psr psr = psrRepository.findById(psrId)
                .orElseThrow(() -> new EntityNotFoundException("PSR not found"));

        TeamOrganization entity = convertToEntity(dto);
        entity.setPsr(psr);
        return convertToDTO(teamOrganizationRepository.save(entity));
    }

    /**
     * Met à jour une organisation d’équipe existante.
     */
    @Override
    public TeamOrganizationDTO updateTeamOrganization(Long id, TeamOrganizationDTO dto) {
        TeamOrganization entity = teamOrganizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TeamOrganization not found"));

        updateEntityFromDTO(entity, dto);
        return convertToDTO(teamOrganizationRepository.save(entity));
    }

    @Override
    public void deleteTeamOrganization(Long id) {
        teamOrganizationRepository.deleteById(id);
    }


    private TeamOrganizationDTO convertToDTO(TeamOrganization entity) {
        TeamOrganizationDTO dto = new TeamOrganizationDTO();
        dto.setId(entity.getId());
        dto.setFullName(entity.getFullName());
        dto.setInitial(entity.getInitial());
        dto.setRole(entity.getRole());
        dto.setProject(entity.getProject());
        dto.setPlannedStartDate(entity.getPlannedStartDate());
        dto.setPlannedEndDate(entity.getPlannedEndDate());
        dto.setAllocation(entity.getAllocation());
        dto.setComingFromTeam(entity.getComingFromTeam());
        dto.setGoingToTeam(entity.getGoingToTeam());
        dto.setHoliday(entity.getHoliday());
        dto.setTeamName(entity.getTeamName());
        return dto;
    }

    private TeamOrganization convertToEntity(TeamOrganizationDTO dto) {
        TeamOrganization entity = new TeamOrganization();
        updateEntityFromDTO(entity, dto);
        return entity;
    }

    private void updateEntityFromDTO(TeamOrganization entity, TeamOrganizationDTO dto) {
        entity.setFullName(dto.getFullName());
        entity.setInitial(dto.getInitial());
        entity.setRole(dto.getRole());
        entity.setProject(dto.getProject());
        entity.setPlannedStartDate(dto.getPlannedStartDate());
        entity.setPlannedEndDate(dto.getPlannedEndDate());
        entity.setAllocation(dto.getAllocation());
        entity.setComingFromTeam(dto.getComingFromTeam());
        entity.setGoingToTeam(dto.getGoingToTeam());
        entity.setHoliday(dto.getHoliday());
        entity.setTeamName(dto.getTeamName());
    }

    /**
     * Récupère tous les membres du projet associé à un PSR avec les dates de la demande.
     */
    @Override
    public List<TeamOrganizationDTO> getAllProjectMembersForPsr(Long psrId) {
        // Récupérer le PSR par son ID
        Psr psr = psrRepository.findById(psrId)
                .orElseThrow(() -> new EntityNotFoundException("PSR not found with ID: " + psrId));

        // Récupérer le projet associé au PSR
        Project project = psr.getProject();
        if (project == null) {
            throw new EntityNotFoundException("No project linked to PSR with ID: " + psrId);
        }

        // Initialiser les dates avec celles du projet par défaut
        LocalDate startDate = project.getStartDate();
        LocalDate endDate = project.getEndDate();

        // Vérifier si le projet a des demandes associées
        if (project.getDemandes() != null && !project.getDemandes().isEmpty()) {
            // Utiliser simplement la première demande du projet
            Demande demandeAssociee = project.getDemandes().get(0);

            // Si on a trouvé une demande associée, utiliser ses dates
            if (demandeAssociee != null) {
                if (demandeAssociee.getDateDebut() != null) {
                    startDate = demandeAssociee.getDateDebut();
                }
                if (demandeAssociee.getDateFin() != null) {
                    endDate = demandeAssociee.getDateFin();
                }
            }
        }

        // Liste qui va contenir les membres d'équipe
        List<TeamOrganizationDTO> members = new ArrayList<>();

        // Parcourir toutes les équipes du projet
        for (Team team : project.getTeams()) {
            // Parcourir tous les membres de chaque équipe
            for (TeamMember member : team.getMembers()) {
                TeamOrganizationDTO dto = new TeamOrganizationDTO();

                // Informations de base du membre
                dto.setId(member.getId());
                dto.setFullName(member.getName());
                dto.setInitial(member.getInitial());
                dto.setRole(member.getRole() != null ? member.getRole().name() : "");
                dto.setProject(project.getName());

                // Définir les dates planifiées (de la demande ou du projet)
                dto.setPlannedStartDate(startDate);
                dto.setPlannedEndDate(endDate);

                // Calculer l'allocation du membre pour ce projet et cette équipe
                dto.setAllocation(getAllocationForMemberAndProject(member.getId(), project.getId(), team.getId()));

                // Déterminer les équipes auxquelles le membre appartient dans ce projet
                String teamsJoined = project.getTeams().stream()
                        .filter(t -> t.getMembers().contains(member))
                        .map(Team::getName)
                        .distinct()
                        .collect(Collectors.joining(", "));

                dto.setComingFromTeam(teamsJoined);
                dto.setGoingToTeam(""); // Champ laissé vide par défaut

                // Gestion des congés
                List<String> holidays = member.getHoliday();
                String holidayCount = holidays != null ? String.valueOf(holidays.size()) : "0";
                dto.setHoliday(holidayCount);


                // Nom de l'équipe actuelle
                dto.setTeamName(team.getName());

                // Ajouter le DTO à la liste
                members.add(dto);
            }
        }

        return members;
    }
    /**
     * Récupère l'allocation d'un membre pour un projet et une équipe spécifiques.
     */
    private String getAllocationForMemberAndProject(Long memberId, Long projectId, Long teamId) {
        if (memberId == null || projectId == null || teamId == null) {
            return "0%";
        }

        List<TeamMemberAllocation> allocations = allocationRepository
                .findAllByTeamMemberIdAndProjectIdAndTeamId(memberId, projectId, teamId);

        if (allocations != null && !allocations.isEmpty()) {
            TeamMemberAllocation allocation = allocations.get(0);
            if (allocation.getAllocation() != null) {
                Double value = allocation.getAllocation();
                int percentage = (int) Math.round(value * 100); // ex: 0.5 → 50
                return percentage + "%";
            }
        }

        return "0%";
    }
}

