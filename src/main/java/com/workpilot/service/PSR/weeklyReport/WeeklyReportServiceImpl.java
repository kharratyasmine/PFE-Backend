package com.workpilot.service.PSR.weeklyReport;

import com.workpilot.dto.PsrDTO.WeeklyReportDTO;
import com.workpilot.entity.PSR.Psr;
import com.workpilot.entity.PSR.WeeklyReport;
import com.workpilot.entity.PSR.TaskTracker;
import com.workpilot.repository.Psr.PsrRepository;
import com.workpilot.repository.Psr.TaskTrackerRepository;
import com.workpilot.repository.Psr.WeeklyReportRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WeeklyReportServiceImpl implements WeeklyReportService  {
    @Autowired
    private WeeklyReportRepository weeklyReportRepository;
    @Autowired
    private TaskTrackerRepository trackerRepository;
    @Autowired
    private PsrRepository psrRepository;

    @Override
    public void generateReportFromPsr(Long psrId) {
        Psr psr = psrRepository.findById(psrId)
                .orElseThrow(() -> new EntityNotFoundException("PSR introuvable"));

        List<TaskTracker> trackers = trackerRepository.findByPsr(psr);

        String weekLabel = psr.getWeek(); // ex: "2025-W22"
        String[] parts = weekLabel.split("-W");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Format de semaine invalide : " + weekLabel);
        }
        int year = Integer.parseInt(parts[0]);
        int week = Integer.parseInt(parts[1]);

        LocalDate reportDate = psr.getReportDate();
        LocalDate monday = reportDate.with(DayOfWeek.MONDAY);
        String month = monday.getMonth().name();

        // Calculer les totaux pour toute la semaine basée sur tous les trackers associés à ce PSR
        double totalWorkedThisWeek = trackers.stream()
                .mapToDouble(t -> t.getWorkedMD() != null ? t.getWorkedMD() : 0)
                .sum();

        double totalEstimatedThisWeek = trackers.stream()
                .mapToDouble(t -> t.getEstimatedMD() != null ? t.getEstimatedMD() : 0)
                .sum();

        double varianceThisWeek = totalEstimatedThisWeek == 0 ? 0 : ((totalWorkedThisWeek - totalEstimatedThisWeek) / totalEstimatedThisWeek) * 100;

        // Construire le nom du projet pour le rapport hebdomadaire consolidé
        String consolidatedProjectName = psr.getProjectName() + " (W" + week + ")";

        // Vérifier si un rapport pour cette semaine et ce PSR existe déjà pour éviter les doublons
        // (Assurez-vous que weeklyReportRepository a une méthode findByWeekNumberAndYearAndPsrId)
        Optional<WeeklyReport> existingReport = weeklyReportRepository.findByWeekNumberAndYearAndPsrId(week, year, psr.getId());

        WeeklyReport report;
        if (existingReport.isPresent()) {
            report = existingReport.get();
            // Mettre à jour le rapport existant
            report.setWorkingDays(totalWorkedThisWeek);
            report.setEstimatedDays(totalEstimatedThisWeek);
            report.setEffortVariance(varianceThisWeek);
            report.setProjectName(consolidatedProjectName);
            report.setWeek(weekLabel);
            report.setMonth(month);
            report.setYear(year);

        } else {
            // Créer un nouveau rapport
            report = WeeklyReport.builder()
                    .month(month)
                    .weekNumber(week)
                    .year(year)
                    .projectName(consolidatedProjectName)
                    .workingDays(totalWorkedThisWeek)
                    .estimatedDays(totalEstimatedThisWeek)
                    .effortVariance(varianceThisWeek)
                    .psr(psr)
                    .week(weekLabel)
                    .build();
        }

        weeklyReportRepository.save(report);
    }


    @Override
    public List<WeeklyReportDTO> getReportsByMonth(String month, int year) {
        return weeklyReportRepository.findByMonthAndYear(month, year).stream().map(r -> WeeklyReportDTO.builder()
                .id(r.getId())
                .month(r.getMonth())
                .weekNumber(r.getWeekNumber())
                .year(r.getYear())
                .projectName(r.getProjectName())
                .workingDays(r.getWorkingDays())
                .estimatedDays(r.getEstimatedDays())
                .effortVariance(r.getEffortVariance())
                .psrId(r.getPsr().getId())
                .build()).collect(Collectors.toList());
    }

    @Override
    public List<WeeklyReportDTO> getReportsByMonthAndPsr(String month, int year, Long psrId) {
        return weeklyReportRepository.findByMonthAndYearAndPsrId(month, year, psrId)
                .stream()
                .map(r -> WeeklyReportDTO.builder()
                        .id(r.getId())
                        .month(r.getMonth())
                        .weekNumber(r.getWeekNumber())
                        .year(r.getYear())
                        .projectName(r.getProjectName())
                        .workingDays(r.getWorkingDays())
                        .estimatedDays(r.getEstimatedDays())
                        .effortVariance(r.getEffortVariance())
                        .psrId(r.getPsr().getId())
                        .build())
                .collect(Collectors.toList());
    }
}