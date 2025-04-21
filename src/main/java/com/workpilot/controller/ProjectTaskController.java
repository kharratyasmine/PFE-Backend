package com.workpilot.controller;

import com.workpilot.dto.ProjectTaskDTO;
import com.workpilot.service.GestionProject.tache.ProjectTaskServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ProjectTaskController {

    private final ProjectTaskServiceImpl taskService;

    @GetMapping
    public ResponseEntity<List<ProjectTaskDTO>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectTaskDTO> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTacheById(id));
    }

    @PostMapping("/project/{projectId}")
    public ResponseEntity<ProjectTaskDTO> createTaskForProject(
            @PathVariable Long projectId,
            @RequestBody ProjectTaskDTO taskDTO) {
        taskDTO.setProjectId(projectId);
        return ResponseEntity.ok(taskService.createTache(taskDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectTaskDTO> updateTask(@PathVariable Long id, @RequestBody ProjectTaskDTO taskDTO) {
        return ResponseEntity.ok(taskService.updateTache(id, taskDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTache(id);
            return ResponseEntity.ok("Tâche supprimée avec succès");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Impossible de supprimer la tâche : elle est liée à d'autres entités.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la suppression de la tâche.");
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ProjectTaskDTO>> getTasksByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getTachesByProject(projectId));
    }
}