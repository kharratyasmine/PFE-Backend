package com.workpilot.controller.DevisController;

import com.workpilot.dto.DevisDTO.WorkloadDetailDTO;
import com.workpilot.entity.devis.WorkloadDetail;
import com.workpilot.service.DevisServices.WorkloadDetail.WorkloadDetailService;
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
        List<WorkloadDetail> workloadDetails = workloadDetailService.GetAllWorkloadDetail();
        return ResponseEntity.ok(workloadDetails);
    }

    @PostMapping
    public ResponseEntity<WorkloadDetail> createWorkloadDetail(@RequestBody WorkloadDetail workloadDetail) {
        return ResponseEntity.ok(workloadDetailService.createWorkloadDetail(workloadDetail));
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
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

    // ✅ Génération manuelle (bouton ou action côté Angular)
    @PostMapping("/generate/{devisId}")
    public ResponseEntity<List<WorkloadDetailDTO>> generate(@PathVariable Long devisId) {
        List<WorkloadDetailDTO> generated = workloadDetailService.generateFromDemandes(devisId);
        return ResponseEntity.ok(generated);
    }

    // ✅ Chargement sans duplication automatique
    @GetMapping("/devis/{devisId}")
    public ResponseEntity<List<WorkloadDetailDTO>> getByDevis(@PathVariable Long devisId) {
        List<WorkloadDetail> existingDetails = workloadDetailService.getByDevisId(devisId);
        List<WorkloadDetailDTO> dtos = existingDetails.stream()
                .map(wd -> WorkloadDetailDTO.builder()
                        .id(wd.getId())
                        .period(wd.getPeriod())
                        .estimatedWorkload(wd.getEstimatedWorkload())
                        .publicHolidays(wd.getPublicHolidays())
                        .publicHolidayDates(wd.getPublicHolidayDates())
                        .numberOfResources(wd.getNumberOfResources())
                        .totalEstimatedWorkload(wd.getTotalEstimatedWorkload())
                        .totalWorkload(wd.getTotalWorkload())
                        .note(wd.getNote())
                        .devisId(wd.getDevis().getId())
                        .build())
                .toList();

        return ResponseEntity.ok(dtos);
    }
}
