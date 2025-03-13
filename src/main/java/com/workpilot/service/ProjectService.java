package com.workpilot.service;

import com.workpilot.entity.Project;

import java.util.List;

public interface ProjectService {
    List<Project> GetAllProject();
    Project getProjectById(Long id);
    Project saveProject(Project project);
    Project updateProject( Long id,Project project);
    void deleteProject(Long id);
}
