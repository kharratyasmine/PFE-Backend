package com.workpilot.controller;

import com.workpilot.dto.TeamMemberAllocationDTO;
import com.workpilot.entity.ressources.TeamMemberAllocation;
import com.workpilot.repository.TeamMemberAllocationRepository;
import com.workpilot.service.GestionProject.allocation.TeamMemberAllocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects/allocations")
@CrossOrigin(origins = "http://localhost:4200")
public class TeamMemberAllocationController {

    private final TeamMemberAllocationService teamMemberAllocationService;
    private final TeamMemberAllocationRepository allocationRepository;

    public TeamMemberAllocationController(TeamMemberAllocationService teamMemberAllocationService,
                                          TeamMemberAllocationRepository allocationRepository) {
        this.teamMemberAllocationService = teamMemberAllocationService;
        this.allocationRepository = allocationRepository;
    }

    /**
     * Récupérer une allocation en fonction de l'ID du membre et du projet.
     * Si aucune allocation n'est trouvée, retourne un DTO avec allocation = 0.
     */
    @GetMapping
    public ResponseEntity<TeamMemberAllocationDTO> getAllocation(
            @RequestParam Long memberId,
            @RequestParam Long projectId) {

        // Récupérer toutes les allocations pour le couple (memberId, projectId)
        List<TeamMemberAllocation> allocations = allocationRepository.findAllByTeamMemberIdAndProjectId(memberId, projectId);

        if (!allocations.isEmpty()) {
            // Exemple : On agrège en sommant les allocations
            double totalAllocation = allocations.stream()
                    .mapToDouble(TeamMemberAllocation::getAllocation)
                    .sum();
            // On choisit, par exemple, de prendre l'ID du premier enregistrement (si utile)
            Long allocationId = allocations.get(0).getId();

            TeamMemberAllocationDTO dto = new TeamMemberAllocationDTO(
                    allocationId,
                    memberId,
                    projectId,
                    totalAllocation
            );
            return ResponseEntity.ok(dto);
        } else {
            // Retourne un DTO indiquant qu'il n'y a aucune allocation (valeur 0)
            TeamMemberAllocationDTO dto = new TeamMemberAllocationDTO(null, memberId, projectId, 0.0);
            return ResponseEntity.ok(dto);
        }
    }

    /**
     * Variante pour obtenir une allocation : retourne 404 si aucune allocation n'est trouvée.
     */
    @GetMapping("/entity")
    public ResponseEntity<TeamMemberAllocationDTO> getAllocationEntity(
            @RequestParam Long memberId,
            @RequestParam Long projectId) {
        List<TeamMemberAllocation> allocations = allocationRepository.findAllByTeamMemberIdAndProjectId(memberId, projectId);
        if (!allocations.isEmpty()) {
            double totalAllocation = allocations.stream()
                    .mapToDouble(TeamMemberAllocation::getAllocation)
                    .sum();
            Long allocationId = allocations.get(0).getId();
            TeamMemberAllocationDTO dto = new TeamMemberAllocationDTO(allocationId, memberId, projectId, totalAllocation);
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Créer une nouvelle allocation.
     */
    @PostMapping
    public ResponseEntity<TeamMemberAllocationDTO> addAllocation(@RequestBody TeamMemberAllocationDTO allocationDTO) {
        TeamMemberAllocationDTO dto = teamMemberAllocationService.addAllocation(allocationDTO);
        return ResponseEntity.ok(dto);
    }

    /**
     * Mettre à jour une allocation existante.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TeamMemberAllocationDTO> updateAllocation(@PathVariable Long id,
                                                                    @RequestBody TeamMemberAllocationDTO allocationDTO) {
        TeamMemberAllocationDTO dto = teamMemberAllocationService.updateAllocation(id, allocationDTO);
        return ResponseEntity.ok(dto);
    }

    /**
     * Supprimer une allocation existante.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAllocation(@PathVariable Long id) {
        teamMemberAllocationService.deleteAllocation(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupérer l'allocation pour un membre et un projet spécifiés.
     */
    @GetMapping("/member/{memberId}/project/{projectId}")
    public ResponseEntity<TeamMemberAllocationDTO> getAllocationByMemberAndProject(@PathVariable Long memberId,
                                                                                   @PathVariable Long projectId) {
        TeamMemberAllocationDTO dto = teamMemberAllocationService.getAllocationByMemberAndProject(memberId, projectId);
        return ResponseEntity.ok(dto);
    }

    /**
     * Récupérer toutes les allocations pour un membre donné.
     */
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<TeamMemberAllocationDTO>> getAllocationsByMember(@PathVariable Long memberId) {
        List<TeamMemberAllocationDTO> dtos = teamMemberAllocationService.getAllocationsByMember(memberId);
        return ResponseEntity.ok(dtos);
    }
}
