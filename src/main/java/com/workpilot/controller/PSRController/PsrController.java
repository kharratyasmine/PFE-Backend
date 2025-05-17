package com.workpilot.controller.PSRController;

import com.workpilot.dto.PsrDTO.PsrDTO;
import com.workpilot.service.PSR.PSR.PsrService;
import com.workpilot.service.PSR.PsrExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/psr")
public class PsrController {

    @Autowired
    private PsrService psrService;
    @Autowired
    private PsrExportService psrExportService;
    // ✅ Créer un PSR
    @PostMapping
    public ResponseEntity<PsrDTO> createPsr(@RequestBody PsrDTO psrDTO) {
        return ResponseEntity.ok(psrService.createPsr(psrDTO));
    }

    // ✅ Obtenir tous les PSR
    @GetMapping
    public ResponseEntity<List<PsrDTO>> getAllPsrs() {
        return ResponseEntity.ok(psrService.getAllPsrs());
    }

    // ✅ Obtenir un PSR par ID
    @GetMapping("/{id}")
    public ResponseEntity<PsrDTO> getPsrById(@PathVariable Long id) {
        return ResponseEntity.ok(psrService.getPsrById(id));
    }

    // ✅ Supprimer un PSR
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePsr(@PathVariable Long id) {
        psrService.deletePsr(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Mettre à jour un PSR
    @PutMapping("/{id}")
    public ResponseEntity<PsrDTO> updatePsr(@PathVariable Long id, @RequestBody PsrDTO psrDTO) {
        return ResponseEntity.ok(psrService.updatePsr(id, psrDTO));
    }

    // ✅ Obtenir tous les PSRs liés à un projet
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<PsrDTO>> getPSRByProjectId(@PathVariable Long projectId) {
        return ResponseEntity.ok(psrService.getPsrsByProject(projectId));
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exportPsr(@PathVariable Long id) {
        try {
            PsrDTO psr = psrService.getPsrById(id);
            ByteArrayInputStream in = psrExportService.exportPsrToExcel(psr);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=psr-report.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(in.readAllBytes());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}
