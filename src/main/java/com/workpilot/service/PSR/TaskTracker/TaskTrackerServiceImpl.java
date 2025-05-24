package com.workpilot.service.PSR.TaskTracker;

import com.workpilot.dto.PsrDTO.TaskTrackerDTO;
import com.workpilot.entity.PSR.Psr;
import com.workpilot.entity.PSR.TaskTracker;
import com.workpilot.entity.ressources.ProjectTask;
import com.workpilot.entity.ressources.TaskAssignment;
import com.workpilot.repository.Psr.PsrRepository;
import com.workpilot.repository.Psr.TaskTrackerRepository;
import com.workpilot.repository.ressources.ProjectTaskRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskTrackerServiceImpl implements TaskTrackerService {

    private final TaskTrackerRepository trackerRepository;
    private final PsrRepository psrRepository;
    private final ProjectTaskRepository projectTaskRepository;

    @Override
    public TaskTrackerDTO save(TaskTrackerDTO dto) {
        Psr psr = psrRepository.findById(dto.getPsrId())
                .orElseThrow(() -> new EntityNotFoundException("PSR introuvable"));

        TaskTracker entity = TaskTracker.builder()
                .id(dto.getId())
                .psr(psr)
                .description(dto.getDescription())
                .week(dto.getWeek())
                .who(dto.getWho())
                .startDate(dto.getStartDate())
                .estimatedEndDate(dto.getEstimatedEndDate())
                .effectiveEndDate(dto.getEffectiveEndDate())
                .workedMD(dto.getWorkedMD())
                .estimatedMD(dto.getEstimatedMD())
                .remainingMD(dto.getRemainingMD())
                .progress(dto.getProgress())
                .currentStatus(dto.getCurrentStatus())
                .effortVariance(dto.getEffortVariance())
                .deviationReason(dto.getDeviationReason())
                .note(dto.getNote())
                .build();

        return convertToDTO(trackerRepository.save(entity));
    }

    @Override
    public List<TaskTrackerDTO> getByPsr(Long psrId) {
        Psr psr = psrRepository.findById(psrId)
                .orElseThrow(() -> new EntityNotFoundException("PSR not found"));

        List<TaskTracker> trackers = trackerRepository.findByPsr(psr);

        if (trackers.isEmpty()) {
            generateFromAssignments(psrId);
            trackers = trackerRepository.findByPsr(psr);
        }

        return trackers.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        trackerRepository.deleteById(id);
    }

    @Override
    public void generateFromAssignments(Long psrId) {
        Psr psr = psrRepository.findById(psrId)
                .orElseThrow(() -> new EntityNotFoundException("PSR introuvable"));

        List<ProjectTask> projectTasks = projectTaskRepository.findByProject(psr.getProject());

        for (ProjectTask task : projectTasks) {
            for (TaskAssignment assign : task.getAssignments()) {
                TaskTracker tracker = TaskTracker.builder()
                        .psr(psr)
                        .description(task.getDescription())
                        .who(assign.getTeamMember().getInitial())
                        .week(getWeekLabel(assign.getEstimatedStartDate()))
                        .startDate(assign.getEstimatedStartDate())
                        .estimatedEndDate(assign.getEstimatedEndDate())
                        .effectiveEndDate(assign.getEffectiveEndDate())
                        .estimatedMD(assign.getEstimatedMD())
                        .workedMD(assign.getWorkedMD())
                        .remainingMD(assign.getRemainingMD())
                        .progress(assign.getProgress())
                        .currentStatus(task.getStatus().toString())
                        .effortVariance(calculateVariance(assign.getEstimatedMD(), assign.getWorkedMD()))
                        .note("")
                        .build();

                trackerRepository.save(tracker);
            }
        }
    }

    private String getWeekLabel(LocalDate date) {
        if (date == null) return "N/A";
        int week = date.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
        return "W" + String.format("%02d", week) + "-" + date.getYear();
    }

    private Double calculateVariance(Double estimated, Double worked) {
        if (estimated == null || estimated == 0) return 0.0;
        return ((worked - estimated) / estimated) * 100.0;
    }

    private TaskTrackerDTO convertToDTO(TaskTracker tracker) {
        return TaskTrackerDTO.builder()
                .id(tracker.getId())
                .psrId(tracker.getPsr().getId())
                .projectId(tracker.getPsr().getProject().getId())
                .description(tracker.getDescription())
                .week(tracker.getWeek())
                .who(tracker.getWho())
                .startDate(tracker.getStartDate())
                .estimatedEndDate(tracker.getEstimatedEndDate())
                .effectiveEndDate(tracker.getEffectiveEndDate())
                .workedMD(tracker.getWorkedMD())
                .estimatedMD(tracker.getEstimatedMD())
                .remainingMD(tracker.getRemainingMD())
                .progress(tracker.getProgress())
                .currentStatus(tracker.getCurrentStatus())
                .effortVariance(tracker.getEffortVariance())
                .deviationReason(tracker.getDeviationReason())
                .note(tracker.getNote())
                .build();
    }

    @Override
    public TaskTrackerDTO update(Long id, TaskTrackerDTO dto) {
        TaskTracker existing = trackerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tâche introuvable avec l'id : " + id));

        existing.setWho(dto.getWho());
        existing.setWeek(dto.getWeek());
        existing.setStartDate(dto.getStartDate());
        existing.setEstimatedEndDate(dto.getEstimatedEndDate());
        existing.setEffectiveEndDate(dto.getEffectiveEndDate());
        existing.setEstimatedMD(dto.getEstimatedMD());
        existing.setWorkedMD(dto.getWorkedMD());
        existing.setRemainingMD(dto.getRemainingMD());
        existing.setProgress(dto.getProgress());
        existing.setCurrentStatus(dto.getCurrentStatus());
        existing.setDeviationReason(dto.getDeviationReason());
        existing.setNote(dto.getNote());

        // Recalculer l'effortVariance si estimé et travaillé changent
        existing.setEffortVariance(calculateVariance(dto.getEstimatedMD(), dto.getWorkedMD()));

        return convertToDTO(trackerRepository.save(existing));
    }

}
