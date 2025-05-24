package com.workpilot.controller.PSRController;

import com.workpilot.dto.PsrDTO.PsrDTO;
import com.workpilot.service.PSR.PSR.PsrService;
import com.workpilot.service.PSR.PsrExportService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

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

    @PostMapping("/project/{projectId}/current-week")
    public ResponseEntity<PsrDTO> createCurrentWeekPsr(@PathVariable Long projectId) {
        return ResponseEntity.ok(psrService.createCurrentWeekPsr(projectId));
    }

    @GetMapping("/project/{projectId}/week-range")
    public ResponseEntity<List<PsrDTO>> getPsrsByWeekRange(
            @PathVariable Long projectId,
            @RequestParam String startWeek,
            @RequestParam String endWeek) {
        return ResponseEntity.ok(psrService.getPsrsByWeekRange(projectId, startWeek, endWeek));
    }

    // Ajouter cette méthode utilitaire
    private String getCurrentWeek() {
        LocalDate now = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekNumber = now.get(weekFields.weekOfWeekBasedYear());
        int year = now.getYear();
        return String.format("%d-W%02d", year, weekNumber);
    }
        // Endpoint pour vérifier l'existence d'un PSR pour la semaine courante
        @GetMapping("/project/{projectId}/check-current-week")
        public ResponseEntity<Boolean> checkCurrentWeekPsr(@PathVariable Long projectId) {
            return ResponseEntity.ok(psrService.existsPsrForCurrentWeek(projectId));
        }

        // Endpoint pour obtenir les PSRs historiques
        @GetMapping("/project/{projectId}/historical")
        public ResponseEntity<List<PsrDTO>> getHistoricalPsrs(
                @PathVariable Long projectId,
                @RequestParam String week) {
            return ResponseEntity.ok(psrService.getHistoricalPsrs(projectId, week));
        }

        // Endpoint pour obtenir le PSR de la semaine courante
        @GetMapping("/project/{projectId}/current")
        public ResponseEntity<PsrDTO> getCurrentWeekPsr(@PathVariable Long projectId) {
            if (psrService.existsPsrForCurrentWeek(projectId)) {
                return ResponseEntity.ok(psrService.getPsrsByProject(projectId)
                        .stream()
                        .filter(psr -> psr.getWeek().equals(getCurrentWeek()))
                        .findFirst()
                        .orElseThrow(() -> new EntityNotFoundException("PSR non trouvé")));
            }
            return ResponseEntity.notFound().build();
        }
    }

