package com.workpilot.controller.PSRController;

import com.workpilot.dto.PsrDTO.WeeklyReportDTO;
import com.workpilot.service.PSR.WeeklyReport.WeeklyReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/weekly-reports")
@RequiredArgsConstructor
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;

    // ✅ Endpoint GET bien défini pour récupérer les rapports
    @GetMapping("/psr/{psrId}/reports")
    public ResponseEntity<List<WeeklyReportDTO>> getWeeklyReportsByPsr(@PathVariable Long psrId) {
        List<WeeklyReportDTO> reports = weeklyReportService.findByPsrId(psrId);
        return ResponseEntity.ok(reports);
    }

    // ✅ Endpoint POST pour générer les rapports automatiquement
    @PostMapping("/psr/{psrId}/generate")
    public ResponseEntity<Void> generateWeekly(@PathVariable Long psrId) {
        weeklyReportService.generateWeeklyReportsByPsr(psrId);
        return ResponseEntity.ok().build();
    }
}
