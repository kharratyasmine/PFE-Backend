package com.workpilot.controller.PSRController;

import com.workpilot.dto.PsrDTO.TaskTrackerDTO;
import com.workpilot.service.PSR.TaskTracker.TaskTrackerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task-tracker")
@RequiredArgsConstructor
public class TaskTrackerController {

    private final TaskTrackerService taskTrackerService;

    @PostMapping
    public ResponseEntity<TaskTrackerDTO> save(@RequestBody TaskTrackerDTO dto) {
        return ResponseEntity.ok(taskTrackerService.save(dto));
    }


    @GetMapping("/psr/{psrId}")
    public ResponseEntity<List<TaskTrackerDTO>> getByPsr(@PathVariable Long psrId) {
        return ResponseEntity.ok(taskTrackerService.getByPsr(psrId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskTrackerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/generate/{psrId}")
    public ResponseEntity<Void> generateFromAssignments(@PathVariable Long psrId) {
        taskTrackerService.generateFromAssignments(psrId);
        return ResponseEntity.ok().build();
    }
    @PatchMapping("/{id}")
    public ResponseEntity<TaskTrackerDTO> updateTask(@PathVariable Long id, @RequestBody TaskTrackerDTO dto) {
        return ResponseEntity.ok(taskTrackerService.update(id, dto));
    }


}

