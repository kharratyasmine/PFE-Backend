package com.workpilot.service.GestionProject.allocation;

import com.workpilot.dto.TeamMemberAllocationDTO;

import com.workpilot.entity.ressources.Project;
import com.workpilot.entity.ressources.TeamMember;
import com.workpilot.entity.ressources.TeamMemberAllocation;
import com.workpilot.repository.TeamMemberAllocationRepository;
import com.workpilot.repository.TeamMemberRepository;
import com.workpilot.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    private ProjectRepository projectRepository;

    public TeamMemberAllocationDTO addAllocation(TeamMemberAllocationDTO allocationDTO) {
        TeamMember member = teamMemberRepository.findById(allocationDTO.getMemberId())
                .orElseThrow(() -> new RuntimeException("Membre non trouvé"));
        Project project = projectRepository.findById(allocationDTO.getProjectId())
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        //✅ Vérifier que la somme des allocations ne dépasse pas 100 %
        double totalAllocation = teamMemberAllocationRepository.findByTeamMemberId(member.getId())
                .stream()
                .mapToDouble(TeamMemberAllocation::getAllocation)
                .sum();

        if (totalAllocation + allocationDTO.getAllocation() > 1.0) {
            throw new RuntimeException("La somme des allocations dépasse 100 %");
        }

        TeamMemberAllocation allocation = new TeamMemberAllocation();
        allocation.setTeamMember(member);
        allocation.setProject(project);
        allocation.setAllocation(allocationDTO.getAllocation());

        allocation = teamMemberAllocationRepository.save(allocation);
        return convertToDTO(allocation);
    }


    @Override
    public TeamMemberAllocationDTO updateAllocation(Long id, TeamMemberAllocationDTO allocationDTO) {
        TeamMemberAllocation allocation = teamMemberAllocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Allocation non trouvée"));

        allocation.setAllocation(allocationDTO.getAllocation());
        allocation = teamMemberAllocationRepository.save(allocation);
        return convertToDTO(allocation);
    }

    @Override
    public void deleteAllocation(Long id) {
        teamMemberAllocationRepository.deleteById(id);
    }

    @Override
    public TeamMemberAllocationDTO getAllocationByMemberAndProject(Long memberId, Long projectId) {
        List<TeamMemberAllocation> allocations = teamMemberAllocationRepository.findAllByTeamMemberIdAndProjectId(memberId, projectId);
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
        dto.setAllocation(allocation.getAllocation());
        return dto;
    }

    private TeamMemberAllocation convertToEntity(TeamMemberAllocationDTO dto) {
        TeamMemberAllocation allocation = new TeamMemberAllocation();
        allocation.setId(dto.getId());
        allocation.setTeamMember(teamMemberRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new RuntimeException("Membre non trouvé")));
        /*allocation.setProject(projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Projet non trouvé")));*/
        allocation.setAllocation(dto.getAllocation());
        return allocation;
    }


    @Override
    public Optional<TeamMemberAllocationDTO> findByTeamMemberAndProject(Long memberId, Long projectId) {
        // Récupérer toutes les allocations pour ce couple membre-projet
        List<TeamMemberAllocation> allocations = teamMemberAllocationRepository.findAllByTeamMemberIdAndProjectId(memberId, projectId);

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
                defaultDTO.setAllocation(0.0);
                return Optional.of(defaultDTO);
            }
            // Si le membre n'est pas rattaché au projet, on renvoie Optional.empty()
            return Optional.empty();
        }
    }



}