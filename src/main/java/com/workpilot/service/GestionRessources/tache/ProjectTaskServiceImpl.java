package com.workpilot.service.GestionRessources.tache;

import com.workpilot.dto.GestionRessources.ProjectTaskDTO;
import com.workpilot.dto.GestionRessources.TaskAssignmentDTO;
import com.workpilot.entity.ressources.*;
import com.workpilot.repository.ressources.*;
import com.workpilot.service.PublicHolidayService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

// ... imports ...

@Service
@RequiredArgsConstructor
public class ProjectTaskServiceImpl implements ProjectTaskService {

    private final ProjectTaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TaskAssignmentRepository assignmentRepository;
    private final PublicHolidayService publicHolidayService;
    private final WorkEntryRepository workEntryRepository;

    @Override
    public ProjectTaskDTO createTache(ProjectTaskDTO dto) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Projet introuvable"));

        ProjectTask task = new ProjectTask();
        task.setName(dto.getName());
        task.setDescription(dto.getDescription());
        task.setDateDebut(dto.getDateDebut());
        task.setDateFin(dto.getDateFin());
        task.setStatus(dto.getStatus());
        task.setProject(project);

        ProjectTask savedTask = taskRepository.save(task);

        if (dto.getAssignments() != null) {
            for (TaskAssignmentDTO assignDTO : dto.getAssignments()) {
                TeamMember member = teamMemberRepository.findById(assignDTO.getTeamMemberId())
                        .orElseThrow(() -> new EntityNotFoundException("Membre introuvable"));

                TaskAssignment assignment = new TaskAssignment();
                assignment.setTask(savedTask);
                assignment.setTeamMember(member);
                assignment.setProgress((int) Math.min(assignDTO.getProgress(), 100.0));

                // 🔍 Calcul à partir des dates effectives
                LocalDate start = assignDTO.getEffectiveStartDate() != null ? assignDTO.getEffectiveStartDate() : assignDTO.getEstimatedStartDate();
                LocalDate end = assignDTO.getEffectiveEndDate() != null ? assignDTO.getEffectiveEndDate() : assignDTO.getEstimatedEndDate();

                Set<LocalDate> publicHolidays = publicHolidayService.getAllCombinedHolidaysBetween(start, end);
                double workedMD = 0.0;
                List<String> personalHolidays = new ArrayList<>();

                if (start != null && end != null && !start.isAfter(end)) {
                    for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                        DayOfWeek day = date.getDayOfWeek();
                        boolean isWeekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
                        boolean isHoliday = publicHolidays.contains(date);
                        boolean workedThisDay = assignDTO.getWorkedDaysList() != null && assignDTO.getWorkedDaysList().contains(date);

                        if (!isWeekend) {
                            if (isHoliday) {
                                // Ne pas enregistrer
                            } else if (workedThisDay) {
                                workedMD += 1.0;
                            } else {
                                personalHolidays.add(date.toString());
                            }
                        }
                    }
                }

                double estimatedMD = calculateWorkedDays(assignDTO.getEstimatedStartDate(), assignDTO.getEstimatedEndDate(), publicHolidays);

                assignment.setEstimatedMD(estimatedMD);
                assignment.setWorkedMD(workedMD);
                assignment.setRemainingMD(Math.max(0, estimatedMD - workedMD));
                assignment.setEstimatedStartDate(assignDTO.getEstimatedStartDate());
                assignment.setEstimatedEndDate(assignDTO.getEstimatedEndDate());
                assignment.setEffectiveStartDate(assignDTO.getEffectiveStartDate());
                assignment.setEffectiveEndDate(assignDTO.getEffectiveEndDate());

                if (member.getHoliday() == null) member.setHoliday(new ArrayList<>());
                Set<String> existing = new HashSet<>(member.getHoliday());
                existing.addAll(personalHolidays);
                member.setHoliday(new ArrayList<>(existing));

                Optional<TaskAssignment> existingTask = assignmentRepository
                        .findByTaskIdAndTeamMemberId(savedTask.getId(), member.getId());

                if (existingTask.isEmpty()) {
                    assignmentRepository.save(assignment);
                } else {
                    System.out.println("⚠️ Assignment déjà existant pour ce membre dans cette tâche");
                }

            }
        }

        return getTacheById(savedTask.getId());
    }

    @Override
    public ProjectTaskDTO updateTache(Long id, ProjectTaskDTO dto) {
        ProjectTask task = taskRepository.findById(id).orElseThrow();
        task.setName(dto.getName());
        task.setDescription(dto.getDescription());
        task.setDateDebut(dto.getDateDebut());
        task.setDateFin(dto.getDateFin());
        task.setStatus(dto.getStatus());
        taskRepository.save(task);

        Map<Long, TaskAssignmentDTO> memberAssignments = new HashMap<>();
        if (dto.getAssignments() != null) {
            for (TaskAssignmentDTO assignDTO : dto.getAssignments()) {
                memberAssignments.put(assignDTO.getTeamMemberId(), assignDTO);
            }
        }

        assignmentRepository.deleteByTaskId(id);

        for (TaskAssignmentDTO assignDTO : memberAssignments.values()) {
            TeamMember member = teamMemberRepository.findById(assignDTO.getTeamMemberId()).orElseThrow();

            TaskAssignment assignment = new TaskAssignment();
            assignment.setTask(task);
            assignment.setTeamMember(member);
            assignment.setProgress((int) Math.min(assignDTO.getProgress(), 100.0));

            LocalDate start = assignDTO.getEffectiveStartDate() != null ? assignDTO.getEffectiveStartDate() : assignDTO.getEstimatedStartDate();
            LocalDate end = assignDTO.getEffectiveEndDate() != null ? assignDTO.getEffectiveEndDate() : assignDTO.getEstimatedEndDate();

            Set<LocalDate> publicHolidays = publicHolidayService.getAllCombinedHolidaysBetween(start, end);
            double workedMD = 0.0;
            List<String> personalHolidays = new ArrayList<>();

            if (start != null && end != null && !start.isAfter(end)) {
                for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                    DayOfWeek day = date.getDayOfWeek();
                    boolean isWeekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
                    boolean isHoliday = publicHolidays.contains(date);
                    boolean workedThisDay = assignDTO.getWorkedDaysList() != null && assignDTO.getWorkedDaysList().contains(date);

                    if (!isWeekend) {
                        if (isHoliday) {
                            // skip
                        } else if (workedThisDay) {
                            workedMD += 1.0;
                        } else {
                            personalHolidays.add(date.toString());
                        }
                    }
                }
            }

            double estimatedMD = calculateWorkedDays(assignDTO.getEstimatedStartDate(), assignDTO.getEstimatedEndDate(), publicHolidays);

            assignment.setEstimatedMD(estimatedMD);
            assignment.setWorkedMD(workedMD);
            assignment.setRemainingMD(Math.max(0, estimatedMD - workedMD));
            assignment.setEstimatedStartDate(assignDTO.getEstimatedStartDate());
            assignment.setEstimatedEndDate(assignDTO.getEstimatedEndDate());
            assignment.setEffectiveStartDate(assignDTO.getEffectiveStartDate());
            assignment.setEffectiveEndDate(assignDTO.getEffectiveEndDate());

            if (member.getHoliday() == null) member.setHoliday(new ArrayList<>());
            Set<String> existing = new HashSet<>(member.getHoliday());
            existing.addAll(personalHolidays);
            member.setHoliday(new ArrayList<>(existing));

            Optional<TaskAssignment> existingTask = assignmentRepository
                    .findByTaskIdAndTeamMemberId(task.getId(), member.getId());

            if (existingTask.isEmpty()) {
                assignmentRepository.save(assignment);
            } else {
                System.out.println("⚠️ Assignment déjà existant pour ce membre dans cette tâche");
            }

        }

        return getTacheById(id);
    }

    @Override
    public ProjectTaskDTO getTacheById(Long id) {
        ProjectTask task = taskRepository.findById(id).orElseThrow();
        ProjectTaskDTO dto = mapToDTO(task);
        List<TaskAssignmentDTO> assignments = assignmentRepository.findByTaskId(id)
                .stream()
                .map(this::mapAssignmentToDTO)
                .collect(Collectors.toList());
        dto.setAssignments(assignments);
        return dto;
    }

    @Override
    public List<ProjectTaskDTO> getTachesByProject(Long projectId) {
        return taskRepository.findByProject_Id(projectId)
                .stream()
                .map(task -> getTacheById(task.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTache(Long id) {
        assignmentRepository.deleteByTaskId(id);
        workEntryRepository.deleteByTaskId(id);
        taskRepository.deleteById(id);
    }

    @Override
    public List<ProjectTaskDTO> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(task -> getTacheById(task.getId()))
                .collect(Collectors.toList());
    }

    private ProjectTaskDTO mapToDTO(ProjectTask task) {
        return new ProjectTaskDTO(
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getDateDebut(),
                task.getDateFin(),
                task.getStatus(),
                task.getProject() != null ? task.getProject().getId() : null,
                task.getProject() != null ? task.getProject().getName() : null,
                null
        );
    }

    private TaskAssignmentDTO mapAssignmentToDTO(TaskAssignment entity) {
        return new TaskAssignmentDTO(
                entity.getId(),
                entity.getTeamMember().getId(),
                entity.getTeamMember().getName(),
                entity.getProgress(),
                entity.getWorkedMD(),
                entity.getEstimatedMD(),
                entity.getRemainingMD(),
                null,
                null,
                entity.getEstimatedStartDate(),
                entity.getEstimatedEndDate(),
                entity.getEffectiveStartDate(),
                entity.getEffectiveEndDate()
        );
    }

    private long calculateWorkedDays(LocalDate start, LocalDate end, Set<LocalDate> holidays) {
        if (start == null || end == null || start.isAfter(end)) return 0;
        long count = 0;
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY && !holidays.contains(date)) {
                count++;
            }
        }
        return count;
    }

    @Override
    @Transactional
    public void updateWorkedMD(Long taskId, Long assignmentId, double workedMD) {
        TaskAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));

        if (!assignment.getTask().getId().equals(taskId)) {
            throw new IllegalArgumentException("Task ID mismatch");
        }

        assignment.setWorkedMD(workedMD);
        assignment.setRemainingMD(assignment.getEstimatedMD() - workedMD);
        assignmentRepository.save(assignment);
    }
}