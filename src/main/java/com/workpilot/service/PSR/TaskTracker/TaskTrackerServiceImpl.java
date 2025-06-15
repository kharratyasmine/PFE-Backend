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
import java.util.Map;
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

        // Synchronisation automatique avant de récupérer les tâches
        synchronizeTaskTrackers(psr);

        List<TaskTracker> trackers = trackerRepository.findByPsr(psr);
        return trackers.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
@Override
public void synchronizeTaskTrackers(Psr psr) {
        String weekLabel = psr.getWeek();
        LocalDate[] weekRange = getStartAndEndOfWeek(weekLabel);
        if (weekRange == null) {
            throw new IllegalArgumentException("Format de semaine invalide : " + weekLabel);
        }

        LocalDate startOfWeek = weekRange[0];
        LocalDate endOfWeek = weekRange[1];

        // Récupérer les trackers existants pour ce PSR
        List<TaskTracker> existingTrackers = trackerRepository.findByPsr(psr);

        // Récupérer toutes les tâches du projet associé au PSR
        List<ProjectTask> projectTasks = projectTaskRepository.findByProject(psr.getProject());

        for (ProjectTask task : projectTasks) {
            for (TaskAssignment assign : task.getAssignments()) {
                LocalDate estimatedStart = assign.getEstimatedStartDate();

                // Vérifier si la tâche est dans la semaine courante
                if (estimatedStart == null || estimatedStart.isBefore(startOfWeek) || estimatedStart.isAfter(endOfWeek)) {
                    continue;
                }

                // Vérifier si un tracker existe déjà pour cette tâche et cet assigné
                boolean exists = existingTrackers.stream()
                        .anyMatch(tracker ->
                                tracker.getDescription().equals(task.getDescription()) &&
                                        tracker.getWho().equals(assign.getTeamMember().getInitial()));

                if (!exists) {
                    // Créer un nouveau tracker
                    TaskTracker tracker = TaskTracker.builder()
                            .psr(psr)
                            .description(task.getDescription())
                            .who(assign.getTeamMember().getInitial())
                            .week(psr.getWeek())
                            .startDate(assign.getEstimatedStartDate())
                            .estimatedEndDate(assign.getEstimatedEndDate())
                            .effectiveEndDate(assign.getEffectiveEndDate())
                            .estimatedMD(assign.getEstimatedMD())
                            .workedMD(assign.getWorkedMD())
                            .remainingMD(assign.getRemainingMD())
                            .progress(assign.getProgress())
                            .currentStatus(task.getStatus().toString())
                            .effortVariance(calculateVariance(assign.getEstimatedMD(), assign.getWorkedMD()))
                            .deviationReason("")
                            .note("")
                            .build();

                    trackerRepository.save(tracker);
                    existingTrackers.add(tracker);
                } else {
                    // Mettre à jour le tracker existant si nécessaire
                    existingTrackers.stream()
                            .filter(tracker ->
                                    tracker.getDescription().equals(task.getDescription()) &&
                                            tracker.getWho().equals(assign.getTeamMember().getInitial()))
                            .findFirst()
                            .ifPresent(tracker -> {
                                // Mettre à jour les champs si nécessaire
                                tracker.setEstimatedEndDate(assign.getEstimatedEndDate());
                                tracker.setEffectiveEndDate(assign.getEffectiveEndDate());
                                tracker.setEstimatedMD(assign.getEstimatedMD());
                                tracker.setWorkedMD(assign.getWorkedMD());
                                tracker.setRemainingMD(assign.getRemainingMD());
                                tracker.setProgress(assign.getProgress());
                                tracker.setCurrentStatus(task.getStatus().toString());
                                tracker.setEffortVariance(calculateVariance(assign.getEstimatedMD(), assign.getWorkedMD()));

                                trackerRepository.save(tracker);
                            });
                }
            }
        }
    }

    // Méthode pour obtenir les tâches groupées par semaine
    public Map<String, List<TaskTrackerDTO>> getTasksGroupedByWeek(Long psrId) {
        List<TaskTrackerDTO> allTasks = getByPsr(psrId);
        return allTasks.stream()
                .collect(Collectors.groupingBy(TaskTrackerDTO::getWeek));
    }

    // Méthode pour obtenir les tâches par projet
    public Map<Long, List<TaskTrackerDTO>> getTasksByProject(Long psrId) {
        List<TaskTrackerDTO> allTasks = getByPsr(psrId);
        return allTasks.stream()
                .collect(Collectors.groupingBy(TaskTrackerDTO::getProjectId));
    }

    // Méthode pour obtenir les tâches par PSR
    public List<TaskTrackerDTO> getTasksByPsr(Long psrId) {
        return getByPsr(psrId);
    }



    @Override
    public void delete(Long id) {
        trackerRepository.deleteById(id);
    }

    @Override
    public void generateFromAssignments(Long psrId) {
        Psr psr = psrRepository.findById(psrId)
                .orElseThrow(() -> new EntityNotFoundException("PSR introuvable"));

        String weekLabel = psr.getWeek();
        LocalDate[] weekRange = getStartAndEndOfWeek(weekLabel);
        if (weekRange == null) {
            throw new IllegalArgumentException("Format de semaine invalide : " + weekLabel);
        }

        LocalDate startOfWeek = weekRange[0];
        LocalDate endOfWeek = weekRange[1];

        List<ProjectTask> projectTasks = projectTaskRepository.findByProject(psr.getProject());

        for (ProjectTask task : projectTasks) {
            for (TaskAssignment assign : task.getAssignments()) {

                LocalDate estimatedStart = assign.getEstimatedStartDate();
                if (estimatedStart == null || estimatedStart.isBefore(startOfWeek) || estimatedStart.isAfter(endOfWeek)) {
                    continue; // on ignore si la tâche ne commence pas dans la semaine du PSR
                }

                TaskTracker tracker = TaskTracker.builder()
                        .psr(psr)
                        .description(task.getDescription())
                        .who(assign.getTeamMember().getInitial())
                        .week(psr.getWeek())
                        .startDate(assign.getEstimatedStartDate())
                        .estimatedEndDate(assign.getEstimatedEndDate())
                        .effectiveEndDate(assign.getEffectiveEndDate())
                        .estimatedMD(assign.getEstimatedMD())
                        .workedMD(assign.getWorkedMD())
                        .remainingMD(assign.getRemainingMD())
                        .progress(assign.getProgress())
                        .currentStatus(task.getStatus().toString())
                        .effortVariance(calculateVariance(assign.getEstimatedMD(), assign.getWorkedMD()))
                        .deviationReason("")
                        .note("")
                        .build();

                trackerRepository.save(tracker);
            }
        }
    }

 /*   private LocalDate[] getStartAndEndOfWeek(String weekLabel) {
        if (weekLabel == null || !weekLabel.startsWith("W")) return null;

        int week = Integer.parseInt(weekLabel.substring(1, 3));
        int year = Integer.parseInt(weekLabel.substring(4));

        LocalDate startOfWeek = LocalDate.ofYearDay(year, 1)
                .with(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
                .with(java.time.DayOfWeek.MONDAY);

        LocalDate endOfWeek = startOfWeek.plusDays(6);

        return new LocalDate[]{startOfWeek, endOfWeek};
    }
*/
 private LocalDate[] getStartAndEndOfWeek(String weekLabel) {
     if (weekLabel == null) return null;

     // Handle format "YYYY-Www"
     if (weekLabel.matches("\\d{4}-W\\d{2}")) {
         String[] parts = weekLabel.split("-W");
         try {
             int year = Integer.parseInt(parts[0]);
             int week = Integer.parseInt(parts[1]);

             // Use Java's WeekFields to correctly handle week numbering based on ISO 8601
             java.time.temporal.WeekFields weekFields = java.time.temporal.WeekFields.ISO;
             LocalDate date = LocalDate.ofYearDay(year, 1)
                     .with(weekFields.weekOfYear(), week)
                     .with(weekFields.dayOfWeek(), java.time.DayOfWeek.MONDAY.getValue());

             return new LocalDate[]{date, date.plusDays(6)};

         } catch (NumberFormatException e) {
             return null; // Should not happen if regex matches, but good practice
         }
     }

     // Handle original format "Www-YYYY" if still needed, or remove if "YYYY-Www" is standard
     // if (weekLabel.matches("W\\d{2}-\\d{4}")) {
     //     // ... original parsing logic ...
     // }


     return null; // Invalid format
 }
    private String getWeekLabel(LocalDate date) {
        if (date == null) return "N/A";
        int week = date.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
        return "W" + String.format("%02d", week) + "-" + date.getYear();
    }

    private Double calculateVariance(Double estimated, Double worked) {
        if (estimated == null || estimated == 0) return 0.0;
        return ((worked - estimated) / estimated);
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

        // Mise à jour des champs
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
        existing.setEffortVariance(calculateVariance(dto.getEstimatedMD(), dto.getWorkedMD()));

        return convertToDTO(trackerRepository.save(existing));
    }


}
