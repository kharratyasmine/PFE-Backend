package com.workpilot.controller;

import com.workpilot.entity.ProjectTask;
import com.workpilot.service.ProjectTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = "*")
public class ProjectTaskController {

    @Autowired
    private ProjectTaskService projectTaskService;

    // 🔹 Obtenir toutes les tâches
    @GetMapping
    public ResponseEntity<List<ProjectTask>> getAllTasks() {
        return ResponseEntity.ok(projectTaskService.getAllTasks());
    }

    // 🔹 Obtenir une tâche par ID
    @GetMapping("/{id}")
    public ResponseEntity<ProjectTask> getTaskById(@PathVariable Long id) {
        Optional<ProjectTask> task = projectTaskService.getTaskById(id);
        return task.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 🔹 Créer une nouvelle tâche
    @PostMapping
    public ResponseEntity<ProjectTask> createTask(@RequestBody ProjectTask task) {
        return ResponseEntity.ok(projectTaskService.createTask(task));
    }

    // 🔹 Mettre à jour une tâche existante
    @PutMapping("/{id}")
    public ResponseEntity<ProjectTask> updateTask(@PathVariable Long id, @RequestBody ProjectTask updatedTask) {
        ProjectTask task = projectTaskService.updateTask(id, updatedTask);
        return ResponseEntity.ok(task);
    }

    // 🔹 Supprimer une tâche
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        projectTaskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
