package com.workpilot.controller.PSRController;

import com.workpilot.dto.PsrDTO.PsrDTO;
import com.workpilot.dto.PsrDTO.RisksDTO;
import com.workpilot.service.PSR.PSR.PsrService;
import com.workpilot.service.PSR.Riskes.RisksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/risks")
@CrossOrigin(origins = "http://localhost:4200")
public class RisksController {

    @Autowired
    private RisksService risksService;

    @Autowired
    private PsrService psrService;

    @PostMapping
    public ResponseEntity<RisksDTO> addRisk(@PathVariable Long psrId, @RequestBody RisksDTO RisksDTO) {
        return ResponseEntity.ok(risksService.addRiskToPsr(psrId, RisksDTO));
    }

    @GetMapping("/psr/{psrId}")
    public ResponseEntity<List<RisksDTO>> getRisks(@PathVariable Long psrId) {
        return ResponseEntity.ok(risksService.getRisksByPsr(psrId));
    }

    @DeleteMapping("/{riskId}")
    public ResponseEntity<Void> deleteRisk(@PathVariable Long riskId) {
        risksService.deleteRisk(riskId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/project/{projectId}")
    public List<PsrDTO> getPsrsByProject(@PathVariable Long projectId) {
        return psrService.getPsrsByProject(projectId);
    }

}
