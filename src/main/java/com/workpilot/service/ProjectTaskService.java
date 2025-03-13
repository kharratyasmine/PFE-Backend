package com.workpilot.service;

import com.workpilot.entity.ProjectTask;

import java.util.List;
import java.util.Optional;

public interface ProjectTaskService {
    List<ProjectTask> getAllTasks();
    Optional<ProjectTask> getTaskById(Long id);
    ProjectTask createTask(ProjectTask task);
    ProjectTask updateTask(Long id, ProjectTask updatedTask);
    void deleteTask(Long id);
}
