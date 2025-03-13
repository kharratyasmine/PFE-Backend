package com.workpilot.controller;

import com.workpilot.entity.Project;
import com.workpilot.entity.WorkloadDetail;
import com.workpilot.service.WorkloadDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workloadDetails")
public class WorkloadDetailController {

    @Autowired
    private WorkloadDetailService workloadDetailService;

    @GetMapping
    public ResponseEntity<List<WorkloadDetail>> GetAllWorkloadDetail() {
        List<WorkloadDetail> workloadDetail = workloadDetailService.GetAllWorkloadDetail();
        return ResponseEntity.ok(workloadDetail);

    }

    @PostMapping
    public ResponseEntity<WorkloadDetail> createWorkloadDetail(@RequestBody WorkloadDetail workloadDetail) {
        return ResponseEntity.ok(workloadDetailService.createWorkloadDetail(workloadDetail));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkloadDetail> updateWorkloadDetail(@PathVariable Long id, @RequestBody WorkloadDetail workloadDetail) {
        return ResponseEntity.ok(workloadDetailService.updateWorkloadDetail(id, workloadDetail));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkloadDetail> getWorkloadDetailById(@PathVariable Long id) {
        return ResponseEntity.ok(workloadDetailService.getWorkloadDetailById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkloadDetail(@PathVariable Long id) {
        workloadDetailService.deleteWorkloadDetail(id);
        return ResponseEntity.noContent().build();
    }
}
