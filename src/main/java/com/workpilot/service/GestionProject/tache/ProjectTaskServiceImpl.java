package com.workpilot.service.GestionProject.tache;

import com.workpilot.dto.ProjectTaskDTO;
import com.workpilot.dto.TaskAssignmentDTO;
import com.workpilot.entity.ressources.*;
import com.workpilot.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectTaskServiceImpl implements ProjectTaskService {

    private final ProjectTaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TaskAssignmentRepository assignmentRepository;

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
        task.setProgress(dto.getProgress());
        task.setProject(project);

        ProjectTask savedTask = taskRepository.save(task);

        if (dto.getAssignments() != null) {
            for (TaskAssignmentDTO assignDTO : dto.getAssignments()) {
                TeamMember member = teamMemberRepository.findById(assignDTO.getTeamMemberId())
                        .orElseThrow(() -> new EntityNotFoundException("Membre introuvable"));

                TaskAssignment assignment = new TaskAssignment();
                assignment.setTask(savedTask);
                assignment.setTeamMember(member);
                assignment.setProgress(assignDTO.getProgress());
                assignment.setWorkedMD(assignDTO.getWorkedMD());
                assignment.setEstimatedMD(assignDTO.getEstimatedMD());
                assignment.setRemainingMD(assignDTO.getRemainingMD());
                assignment.setEstimatedStartDate(assignDTO.getEstimatedStartDate());
                assignment.setEstimatedEndDate(assignDTO.getEstimatedEndDate());
                assignment.setEffectiveStartDate(assignDTO.getEffectiveStartDate());
                assignment.setEffectiveEndDate(assignDTO.getEffectiveEndDate());
                assignmentRepository.save(assignment);
            }
        }

        return getTacheById(savedTask.getId());
    }

    @Override
    public ProjectTaskDTO getTacheById(Long id) {
        ProjectTask task = taskRepository.findById(id).orElseThrow();
        ProjectTaskDTO dto = mapToDTO(task);

        List<TaskAssignmentDTO> assignments = assignmentRepository.findByTaskId(id).stream()
                .map(this::mapAssignmentToDTO)
                .collect(Collectors.toList());
        dto.setAssignments(assignments);

        return dto;
    }

    @Override
    public List<ProjectTaskDTO> getTachesByProject(Long projectId) {
        return taskRepository.findByProject_Id(projectId).stream()
                .map(task -> getTacheById(task.getId()))
                .collect(Collectors.toList());
    }


    @Override
    public void deleteTache(Long id) {
        assignmentRepository.deleteByTaskId(id);
        taskRepository.deleteById(id);
    }

    @Override
    public ProjectTaskDTO updateTache(Long id, ProjectTaskDTO dto) {
        ProjectTask task = taskRepository.findById(id).orElseThrow();

        task.setName(dto.getName());
        task.setDescription(dto.getDescription());
        task.setDateDebut(dto.getDateDebut());
        task.setDateFin(dto.getDateFin());
        task.setStatus(dto.getStatus());
        task.setProgress(dto.getProgress());
        taskRepository.save(task);

        assignmentRepository.deleteByTaskId(id);

        if (dto.getAssignments() != null) {
            for (TaskAssignmentDTO assignDTO : dto.getAssignments()) {
                TeamMember member = teamMemberRepository.findById(assignDTO.getTeamMemberId()).orElseThrow();

                TaskAssignment assignment = new TaskAssignment();
                assignment.setTask(task);
                assignment.setTeamMember(member);
                assignment.setProgress(assignDTO.getProgress());
                assignment.setWorkedMD(assignDTO.getWorkedMD());
                assignment.setEstimatedMD(assignDTO.getEstimatedMD());
                assignment.setRemainingMD(assignDTO.getRemainingMD());
                assignment.setEstimatedStartDate(assignDTO.getEstimatedStartDate());
                assignment.setEstimatedEndDate(assignDTO.getEstimatedEndDate());
                assignment.setEffectiveStartDate(assignDTO.getEffectiveStartDate());
                assignment.setEffectiveEndDate(assignDTO.getEffectiveEndDate());
                assignmentRepository.save(assignment);
            }
        }

        return getTacheById(id);
    }

    @Override
    public List<ProjectTaskDTO> getAllTasks() {
        return taskRepository.findAll().stream()
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
                task.getProgress(),
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
                entity.getEstimatedStartDate(),
                entity.getEstimatedEndDate(),
                entity.getEffectiveStartDate(),
                entity.getEffectiveEndDate()
        );
    }
}
