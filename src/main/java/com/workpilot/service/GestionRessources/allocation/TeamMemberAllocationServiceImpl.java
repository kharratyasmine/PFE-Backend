package com.workpilot.service.GestionRessources.allocation;

import com.workpilot.dto.GestionRessources.PlannedWorkloadMemberDTO;
import com.workpilot.dto.GestionRessources.TeamMemberAllocationDTO;

import com.workpilot.entity.ressources.*;
import com.workpilot.repository.ressources.TeamMemberAllocationRepository;
import com.workpilot.repository.ressources.TeamMemberRepository;
import com.workpilot.repository.ressources.ProjectRepository;
import com.workpilot.repository.ressources.TeamRepository;
import com.workpilot.service.GestionRessources.PlannedWorkloadMember.PlannedWorkloadMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TeamMemberAllocationServiceImpl implements TeamMemberAllocationService {

    @Autowired
    private TeamMemberAllocationRepository teamMemberAllocationRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PlannedWorkloadMemberService plannedWorkloadMemberService;


    @Override
    public TeamMemberAllocationDTO addAllocation(TeamMemberAllocationDTO allocationDTO) {
        TeamMember member = teamMemberRepository.findById(allocationDTO.getMemberId())
                .orElseThrow(() -> new RuntimeException("Membre non trouvé"));
        Project project = projectRepository.findById(allocationDTO.getProjectId())
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        Team team = project.getTeams().stream()
                .filter(t -> t.getMembers().contains(member))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aucune équipe trouvée pour ce membre dans ce projet"));

        Optional<TeamMemberAllocation> existingOpt =
                teamMemberAllocationRepository.findAllByTeamMemberIdAndProjectIdAndTeamId(member.getId(), project.getId(), team.getId())
                        .stream().findFirst();

        TeamMemberAllocation allocation = existingOpt.orElse(new TeamMemberAllocation());
        allocation.setTeamMember(member);
        allocation.setProject(project);
        allocation.setTeam(team);
        allocation.setAllocation(allocationDTO.getAllocation());

        allocation = teamMemberAllocationRepository.save(allocation);

        // ✨ 🔥 GÉNÉRER AUTOMATIQUEMENT LE PLANNING APRÈS ALLOCATION
        plannedWorkloadMemberService.generateForMember(project.getId(), member.getId(), allocation.getAllocation());

        return convertToDTO(allocation);
    }





    @Override
    public TeamMemberAllocationDTO updateAllocation(Long id, TeamMemberAllocationDTO allocationDTO) {
        // 🔍 Récupération de l'allocation existante
        TeamMemberAllocation allocation = teamMemberAllocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Allocation non trouvée"));

        // 💾 Mise à jour de la valeur d'allocation
        allocation.setAllocation(allocationDTO.getAllocation());
        teamMemberAllocationRepository.save(allocation);

        Long projectId = allocation.getProject().getId();
        Long memberId = allocation.getTeamMember().getId();

        // 🧹 Étape 1 : Supprimer les anciens workloads associés
        plannedWorkloadMemberService.deleteWorkloadsByProjectAndMember(projectId, memberId);

        // 🔄 Étape 2 : Régénérer les workloads avec la nouvelle allocation
        plannedWorkloadMemberService.generateForMember(projectId, memberId, allocation.getAllocation());

        // 🔁 Recharge et retour
        TeamMemberAllocation updated = teamMemberAllocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Erreur lors du rechargement de l'allocation"));

        return convertToDTO(updated);
    }






    @Override
    public void deleteAllocation(Long id) {
        TeamMemberAllocation allocation = teamMemberAllocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Allocation non trouvée"));

        Long projectId = allocation.getProject().getId();
        Long memberId = allocation.getTeamMember().getId();

        // Supprimer uniquement les workloads dans la période des demandes
        List<PlannedWorkloadMemberDTO> workloads = plannedWorkloadMemberService.getByMember(memberId, projectId);

        List<Demande> demandes = allocation.getProject().getDemandes();
        if (demandes != null && !demandes.isEmpty()) {
            for (Demande demande : demandes) {
                LocalDate current = demande.getDateDebut().withDayOfMonth(1);
                LocalDate end = demande.getDateFin().withDayOfMonth(1);

                while (!current.isAfter(end)) {
                    int year = current.getYear();
                    int month = current.getMonthValue();

                    Optional<PlannedWorkloadMemberDTO> toDelete = workloads.stream()
                            .filter(w -> w.getYear() == year && w.getMonth() == month)
                            .findFirst();

                    toDelete.ifPresent(dto -> plannedWorkloadMemberService.delete(dto.getId()));

                    current = current.plusMonths(1);
                }
            }
        }

        // Enfin, supprimer l'allocation
        teamMemberAllocationRepository.deleteById(id);
    }



    @Override
    public TeamMemberAllocationDTO getAllocationByMemberAndProjectAndTeam(Long memberId, Long projectId,Long teamId) {
        List<TeamMemberAllocation> allocations = teamMemberAllocationRepository.findAllByTeamMemberIdAndProjectIdAndTeamId(memberId, projectId,teamId);
        if (allocations.isEmpty()) {
            throw new RuntimeException("Allocation non trouvée");
        }
        // Somme des allocations pour ce membre et ce projet
        double totalAllocation = allocations.stream()
                .mapToDouble(TeamMemberAllocation::getAllocation)
                .sum();
        // Par exemple, on peut utiliser l'ID du premier enregistrement (si besoin)
        Long allocationId = allocations.get(0).getId();

        return new TeamMemberAllocationDTO(allocationId, memberId, projectId, totalAllocation);
    }


    @Override
    public List<TeamMemberAllocationDTO> getAllocationsByMember(Long memberId) {
        List<TeamMemberAllocation> allocations = teamMemberAllocationRepository.findByTeamMemberId(memberId);
        return allocations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }



    private TeamMemberAllocationDTO convertToDTO(TeamMemberAllocation allocation) {
        TeamMemberAllocationDTO dto = new TeamMemberAllocationDTO();
        dto.setId(allocation.getId());
        dto.setMemberId(allocation.getTeamMember().getId());
        dto.setProjectId(allocation.getProject().getId());
        dto.setTeamId(allocation.getTeam().getId());
        dto.setAllocation(allocation.getAllocation());
        return dto;
    }

    private TeamMemberAllocation convertToEntity(TeamMemberAllocationDTO dto) {
        TeamMemberAllocation allocation = new TeamMemberAllocation();
        allocation.setId(dto.getId());
        allocation.setTeamMember(teamMemberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new RuntimeException("Membre non trouvé")));
        allocation.setAllocation(dto.getAllocation());
        return allocation;
    }


    @Override
    public Optional<TeamMemberAllocationDTO> findByTeamMemberAndProjectAndTeam(Long memberId, Long projectId , Long teamId) {
        // Récupérer toutes les allocations pour ce couple membre-projet
        List<TeamMemberAllocation> allocations = teamMemberAllocationRepository.findAllByTeamMemberIdAndProjectIdAndTeamId(memberId, projectId , teamId);

        if (!allocations.isEmpty()) {
            // On agrège les valeurs, ici nous faisons la somme des allocations
            double totalAllocation = allocations.stream()
                    .mapToDouble(TeamMemberAllocation::getAllocation)
                    .sum();
            // On peut conserver l'ID du premier enregistrement si nécessaire
            Long allocationId = allocations.get(0).getId();
            TeamMemberAllocationDTO dto = new TeamMemberAllocationDTO(allocationId, memberId, projectId, totalAllocation);
            return Optional.of(dto);
        } else {
            // Si aucune allocation n'est trouvée, on vérifie si le membre appartient aux équipes affectées au projet
            TeamMember member = teamMemberRepository.findById(memberId)
                    .orElseThrow(() -> new RuntimeException("Membre introuvable"));
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Projet introuvable"));
            boolean isMemberInProjectTeams = project.getTeams().stream()
                    .anyMatch(team -> team.getMembers().contains(member));

            if (isMemberInProjectTeams) {
                // Retourne une allocation par défaut de 0.0 si le membre est lié au projet
                TeamMemberAllocationDTO defaultDTO = new TeamMemberAllocationDTO();
                defaultDTO.setMemberId(memberId);
                defaultDTO.setProjectId(projectId);
                defaultDTO.setTeamId(teamId);
                defaultDTO.setAllocation(0.0);
                return Optional.of(defaultDTO);
            }
            // Si le membre n'est pas rattaché au projet, on renvoie Optional.empty()
            return Optional.empty();
        }
    }



}