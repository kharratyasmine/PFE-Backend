package com.workpilot.controller.Dashboard;

import com.workpilot.dto.Dashboard.PlannedWorkloadDTO;
import com.workpilot.service.Dashboard.PlannedWorkload.PlannedWorkloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/planned-workload")
@RequiredArgsConstructor
public class PlannedWorkloadController {

    private final PlannedWorkloadService service;

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<PlannedWorkloadDTO>> getByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(service.getByProjectId(projectId));
    }

    @PostMapping
    public ResponseEntity<PlannedWorkloadDTO> save(@RequestBody PlannedWorkloadDTO dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
