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
    @Override
    public List<TeamOrganizationDTO> getAll() {
        return teamOrganizationRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    @Override
    public List<TeamOrganizationDTO> getTeamByPsrId(Long psrId) {
        return teamOrganizationRepository.findByPsrId(psrId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    @Override
    public TeamOrganizationDTO createTeamOrganization(Long psrId, TeamOrganizationDTO dto) {
        Psr psr = psrRepository.findById(psrId)
                .orElseThrow(() -> new EntityNotFoundException("PSR not found"));

        TeamOrganization entity = convertToEntity(dto);
        entity.setPsr(psr);
        return convertToDTO(teamOrganizationRepository.save(entity));
    }
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
    @Override
    public List<TeamOrganizationDTO> getAllProjectMembersForPsr(Long psrId) {
        Psr psr = psrRepository.findById(psrId)
                .orElseThrow(() -> new EntityNotFoundException("PSR not found with ID: " + psrId));

        Project project = psr.getProject();
        if (project == null) {
            throw new EntityNotFoundException("No project linked to PSR with ID: " + psrId);
        }

        LocalDate startDate = project.getStartDate();
        LocalDate endDate = project.getEndDate();

        if (project.getDemandes() != null && !project.getDemandes().isEmpty()) {
            Demande demande = project.getDemandes().get(0);
            if (demande.getDateDebut() != null) startDate = demande.getDateDebut();
            if (demande.getDateFin() != null) endDate = demande.getDateFin();
        }

        List<TeamOrganizationDTO> result = new ArrayList<>();

        for (Team team : project.getTeams()) {
            for (TeamMember member : team.getMembers()) {
                TeamOrganizationDTO dto = new TeamOrganizationDTO();

                dto.setFullName(member.getName());
                dto.setInitial(member.getInitial());
                dto.setRole(member.getRole() != null ? member.getRole().name() : "");
                dto.setProject(project.getName());
                dto.setPlannedStartDate(startDate);
                dto.setPlannedEndDate(endDate);
                dto.setAllocation(getAllocationForMemberAndProject(member.getId(), project.getId(), team.getId()));

                String teamName = team.getName();
                dto.setComingFromTeam("");
                dto.setGoingToTeam("");
                dto.setTeamName(teamName);

                List<String> holidays = member.getHoliday();
                dto.setHoliday(holidays != null ? String.join(", ", holidays) : "");

                // ✅ convert DTO to entity and link PSR
                TeamOrganization entity = convertToEntity(dto);
                entity.setPsr(psr);

                // ✅ vérifier doublon : on évite de créer si déjà enregistré
                boolean alreadyExists = teamOrganizationRepository.existsByPsrIdAndInitialAndFullName(psrId, dto.getInitial(), dto.getFullName());
                if (!alreadyExists) {
                    teamOrganizationRepository.save(entity);
                }

                result.add(convertToDTO(entity));
            }
        }

        return result;
    }

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

        @Override
        public List<TeamOrganizationDTO> getTeamByPsrIdAndWeek(Long psrId, String week) {
            // 1. Vérifier si le PSR existe
            Psr psr = psrRepository.findById(psrId)
                    .orElseThrow(() -> new EntityNotFoundException("PSR not found with ID: " + psrId));

            // 2. Récupérer tous les membres de l'équipe pour ce PSR
            List<TeamOrganization> allTeamMembers = teamOrganizationRepository.findByPsrId(psrId);

            // 3. Filtrer pour ne garder que les données de la semaine spécifiée
            List<TeamOrganization> weekSpecificData = allTeamMembers.stream()
                    .filter(member -> week.equals(member.getWeek()))
                    .collect(Collectors.toList());

            // 4. Si aucun membre n'a de données pour cette semaine, on récupère les membres de base
            if (weekSpecificData.isEmpty()) {
                List<TeamOrganizationDTO> baseMembers = getAllProjectMembersForPsr(psrId);

                // 5. Pour chaque membre, on crée une entrée pour cette semaine
                for (TeamOrganizationDTO member : baseMembers) {
                    member.setWeek(week);
                    member.setReportYear(Integer.parseInt(week.split("-")[0]));

                    // 6. Sauvegarder les données pour cette semaine
                    TeamOrganization entity = convertToEntity(member);
                    entity.setPsr(psr);
                    entity.setWeek(week);
                    entity.setReportYear(Integer.parseInt(week.split("-")[0]));
                    teamOrganizationRepository.save(entity);
                }

                return baseMembers;
            }

            // 7. Retourner les données filtrées pour cette semaine
            return weekSpecificData.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

}