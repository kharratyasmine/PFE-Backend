package com.workpilot.service.GestionProject.tache;

import com.workpilot.dto.ProjectTaskDTO;

import java.util.List;

public interface ProjectTaskService {
  List<ProjectTaskDTO> getAllTasks();
    ProjectTaskDTO createTache(ProjectTaskDTO dto);
    ProjectTaskDTO getTacheById(Long id);
    ProjectTaskDTO updateTache(Long id, ProjectTaskDTO dto);
    void deleteTache(Long id);
    List<ProjectTaskDTO> getTachesByProject(Long projectId);
}
