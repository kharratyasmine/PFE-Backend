package com.workpilot.controller;

import com.workpilot.dto.PlannedWorkloadDTO;
import com.workpilot.service.GestionProject.PlannedWorkload.PlannedWorkloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/planning")
@CrossOrigin(origins = "http://localhost:4200")
public class PlannedWorkloadController {

    private final PlannedWorkloadService service;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<PlannedWorkloadDTO>> getByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(service.getByProject(projectId));
    }

    @PostMapping
    public ResponseEntity<PlannedWorkloadDTO> create(@RequestBody PlannedWorkloadDTO dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlannedWorkloadDTO> update(@PathVariable Long id, @RequestBody PlannedWorkloadDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}