package com.workpilot.controller.GestionRessources;

import com.workpilot.dto.GestionRessources.ProjectDTO;
import com.workpilot.dto.GestionRessources.TeamAllocationDTO;
import com.workpilot.dto.GestionRessources.TeamMemberAllocationDTO;
import com.workpilot.dto.GestionRessources.TeamMemberDTO;
import com.workpilot.entity.ressources.Project;
import com.workpilot.repository.ressources.ProjectRepository;
import com.workpilot.service.GestionRessources.project.ProjectService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@CrossOrigin(origins = "http://localhost:4200")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        return ResponseEntity.ok(projectService.GetAllProject());
    }

    @GetMapping("/{id}")
    public ProjectDTO getProjectById(@PathVariable Long id) {
        Project project = projectService.getProjectById(id);
        return projectService.convertToDTO(project);
    }

    @GetMapping("/{projectId}/members")
    public ResponseEntity<List<TeamMemberDTO>> getMembersByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getMembersByProject(projectId));
    }


    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody ProjectDTO dto) {
        System.out.println("📌 DTO reçu : " + dto);

        if (dto.getClientId() == null) {
            return ResponseEntity.badRequest().body("Client ID est manquant !");
        }

        if (dto.getUserId() == null) {
            return ResponseEntity.badRequest().body("User ID est manquant !");
        }

        Project savedProject = projectService.createProject(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProject);
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(@PathVariable Long id, @RequestBody ProjectDTO dto) {
        try {
            ProjectDTO  updatedProject = projectService.updateProject(id, dto);
            return ResponseEntity.ok(updatedProject);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{projectId}/add-team/{teamId}")
    public ResponseEntity<String> assignTeamToProject(
            @PathVariable Long projectId,
            @PathVariable Long teamId) {

        projectService.assignTeamToProject(projectId, teamId);
        return ResponseEntity.ok("✅ Équipe ajoutée avec succès au projet.");
    }

    @GetMapping("/{id}/allocations")
    public ResponseEntity<List<TeamMemberAllocationDTO>> getProjectAllocations(@PathVariable Long id) {
        List<TeamMemberAllocationDTO> allocations = projectService.getAllocationsByProjectId(id);
        return ResponseEntity.ok(allocations);
    }

    @GetMapping("/{id}/team-allocations")
    public ResponseEntity<List<TeamAllocationDTO>> getTeamAllocations(@PathVariable Long id) {
        List<TeamAllocationDTO> allocations = projectService.getTeamAllocationsByProjectId(id);
        return ResponseEntity.ok(allocations);
    }



    @DeleteMapping("/{projectId}/teams/{teamId}")
    public ResponseEntity<String> removeTeamFromProject(@PathVariable Long projectId, @PathVariable Long teamId) {
        projectService.removeTeamFromProject(projectId, teamId);
        return ResponseEntity.ok("Équipe supprimée du projet");
    }

}
