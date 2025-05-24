package com.workpilot.service.PSR.WeeklyReport;

import com.workpilot.dto.PsrDTO.WeeklyReportDTO;
import com.workpilot.entity.PSR.Psr;
import com.workpilot.entity.PSR.WeeklyReport;
import com.workpilot.entity.ressources.ProjectTask;
import com.workpilot.entity.ressources.TaskAssignment;
import com.workpilot.repository.Psr.PsrRepository;
import com.workpilot.repository.Psr.WeeklyReportRepository;
import com.workpilot.repository.ressources.ProjectTaskRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WeeklyReportServiceImpl implements WeeklyReportService {

    private final WeeklyReportRepository reportRepo;
    private final PsrRepository psrRepo;
    private final ProjectTaskRepository taskRepository;

    @Override
    public WeeklyReportDTO save(WeeklyReportDTO dto) {
        Psr psr = psrRepo.findById(dto.getPsrId())
                .orElseThrow(() -> new EntityNotFoundException("PSR not found"));

        WeeklyReport entity = new WeeklyReport();
        entity.setPsr(psr);
        entity.setProjectName(dto.getProjectName());
        entity.setWorkingDays(dto.getWorkingDays());
        entity.setEstimatedDays(dto.getEstimatedDays());
        entity.setEffortVariance(dto.getEffortVariance());
        entity.setWeek(dto.getWeek());

        WeeklyReport saved = reportRepo.save(entity);
        return convertToDTO(saved);
    }

    @Override
    public List<WeeklyReportDTO> findByPsrId(Long psrId) {
        return reportRepo.findByPsrId(psrId).stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    public void delete(Long id) {
        reportRepo.deleteById(id);
    }

    @Override
    public void generateWeeklyReportsByPsr(Long psrId) {
        Psr psr = psrRepo.findById(psrId)
                .orElseThrow(() -> new EntityNotFoundException("PSR introuvable"));

        List<ProjectTask> tasks = taskRepository.findByProject(psr.getProject());

        Map<String, WeeklyReportDTO> weeklyMap = new HashMap<>();

        for (ProjectTask task : tasks) {
            for (TaskAssignment assignment : task.getAssignments()) {
                String weekLabel = getWeekLabel(assignment.getEstimatedStartDate());

                WeeklyReportDTO dto = weeklyMap.getOrDefault(weekLabel, new WeeklyReportDTO());
                dto.setWeek(weekLabel);
                dto.setProjectName(psr.getProject().getName());
                dto.setPsrId(psrId);

                double worked = assignment.getWorkedMD();
                double estimated = assignment.getEstimatedMD();

                dto.setWorkingDays(dto.getWorkingDays() != null ? dto.getWorkingDays() + worked : worked);
                dto.setEstimatedDays(dto.getEstimatedDays() != null ? dto.getEstimatedDays() + estimated : estimated);

                weeklyMap.put(weekLabel, dto);
            }
        }

        // Supprimer les anciens rapports liés à ce PSR
        reportRepo.deleteByPsr(psr);

        // Sauvegarder les nouveaux rapports
        for (WeeklyReportDTO dto : weeklyMap.values()) {
            WeeklyReport report = convertToEntity(dto, psr);
            reportRepo.save(report);
        }
    }

    private String getWeekLabel(LocalDate date) {
        if (date == null) return "Semaine inconnue";
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekNumber = date.get(weekFields.weekOfWeekBasedYear());
        int year = date.getYear();
        return String.format("W%02d-%d", weekNumber, year);
    }

    private WeeklyReport convertToEntity(WeeklyReportDTO dto, Psr psr) {
        WeeklyReport report = new WeeklyReport();
        report.setPsr(psr);
        report.setProjectName(dto.getProjectName());
        report.setWeek(dto.getWeek());
        report.setWorkingDays(dto.getWorkingDays());
        report.setEstimatedDays(dto.getEstimatedDays());

        // Calcul de l’écart d’effort
        if (dto.getEstimatedDays() != null && dto.getEstimatedDays() > 0) {
            double variance = ((dto.getWorkingDays() - dto.getEstimatedDays()) / dto.getEstimatedDays()) * 100;
            report.setEffortVariance(variance);
        } else {
            report.setEffortVariance(0.0);
        }

        return report;
    }

    private WeeklyReportDTO convertToDTO(WeeklyReport report) {
        return WeeklyReportDTO.builder()
                .id(report.getId())
                .psrId(report.getPsr().getId())
                .projectName(report.getProjectName())
                .workingDays(report.getWorkingDays())
                .estimatedDays(report.getEstimatedDays())
                .effortVariance(report.getEffortVariance())
                .week(report.getWeek())
                .build();
    }
}
