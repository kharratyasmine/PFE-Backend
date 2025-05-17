package com.workpilot.controller.GestionRessources;

import com.workpilot.dto.GestionRessources.PlannedWorkloadMemberDTO;

import com.workpilot.service.GestionRessources.PlannedWorkloadMember.PlannedWorkloadMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/plannedWorkloadMember")
public class PlannedWorkloadMemberController {

    @Autowired
    private PlannedWorkloadMemberService service;

    // 🔹 GET by Project and Year
    @GetMapping("/project/{projectId}/year/{year}")
    public ResponseEntity<List<PlannedWorkloadMemberDTO>> getWorkloadsByProjectAndYear(
            @PathVariable Long projectId,
            @PathVariable int year) {
        return ResponseEntity.ok(service.getByProjectAndYear(projectId, year));
    }

    // 🔹 POST (Create)
    @PostMapping
    public ResponseEntity<PlannedWorkloadMemberDTO> create(@RequestBody PlannedWorkloadMemberDTO dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    // 🔹 PUT (Update)
    @PutMapping("/{id}")
    public ResponseEntity<PlannedWorkloadMemberDTO> update(@PathVariable Long id, @RequestBody PlannedWorkloadMemberDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    // 🔹 DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/member/{projectId}/{memberId}")
    public List<PlannedWorkloadMemberDTO> getByMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId) {
        return service.getByMember(memberId, projectId);
    }
    @PostMapping("/bulk")
    public ResponseEntity<Void> bulkSave(@RequestBody List<PlannedWorkloadMemberDTO> workloads) {
        for (PlannedWorkloadMemberDTO dto : workloads) {
            service.save(dto); // 🔵 soit créer soit mettre à jour
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate/{projectId}")
    public ResponseEntity<List<PlannedWorkloadMemberDTO>> generateWorkloadsForProject(
            @PathVariable Long projectId) {

        List<PlannedWorkloadMemberDTO> generated = service.generateWorkloadsForProject(projectId);

        return ResponseEntity.ok(generated);
    }



}
