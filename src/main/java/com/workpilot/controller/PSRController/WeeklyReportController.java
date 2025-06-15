package com.workpilot.controller.PSRController;

import com.workpilot.dto.PsrDTO.WeeklyReportDTO;
import com.workpilot.service.PSR.weeklyReport.WeeklyReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weekly-reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class WeeklyReportController {

    private final WeeklyReportService reportService;

    @PostMapping("/generate/{psrId}")
    public void generate(@PathVariable Long psrId) {
        reportService.generateReportFromPsr(psrId);
    }

    @GetMapping
    public List<WeeklyReportDTO> getWeeklyReports(
            @RequestParam String month,
            @RequestParam int year,
            @RequestParam(required = false) Long psrId) {
        if (psrId != null) {
            return reportService.getReportsByMonthAndPsr(month, year, psrId);
        } else {
            return reportService.getReportsByMonth(month, year);
        }
    }
}