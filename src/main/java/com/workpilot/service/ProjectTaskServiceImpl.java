package com.workpilot.service.impl;

import com.workpilot.entity.ProjectTask;
import com.workpilot.repository.ProjectTaskRepository;
import com.workpilot.service.ProjectTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectTaskServiceImpl implements ProjectTaskService {

    @Autowired
    private ProjectTaskRepository projectTaskRepository;

    @Override
    public List<ProjectTask> getAllTasks() {
        return projectTaskRepository.findAll();
    }

    @Override
    public Optional<ProjectTask> getTaskById(Long id) {
        return projectTaskRepository.findById(id);
    }

    @Override
    public ProjectTask createTask(ProjectTask task) {
        return projectTaskRepository.save(task);
    }

    @Override
    public ProjectTask updateTask(Long id, ProjectTask updatedTask) {
        return projectTaskRepository.findById(id).map(existingTask -> {
            existingTask.setName(updatedTask.getName());
            existingTask.setDescription(updatedTask.getDescription());
            existingTask.setDateDebut(updatedTask.getDateDebut());
            existingTask.setDateFin(updatedTask.getDateFin());
            existingTask.setStatut(updatedTask.getStatut());
            existingTask.setProject(updatedTask.getProject());
            existingTask.setTeamMember(updatedTask.getTeamMember());
            return projectTaskRepository.save(existingTask);
        }).orElseThrow(() -> new RuntimeException("Task not found"));
    }

    @Override
    public void deleteTask(Long id) {
        projectTaskRepository.deleteById(id);
    }
}
