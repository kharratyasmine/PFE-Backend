package com.workpilot.service;

import com.workpilot.entity.Project;
import com.workpilot.entity.Team;
import com.workpilot.exception.DevisNotFoundException;
import com.workpilot.repository.ProjectRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

@Autowired
    private  ProjectRepository projectRepository;

    @Override
    public List<Project> GetAllProject() {
        List<Project> projects = projectRepository.findAll();
        for (Project p : projects) {
            p.getTeams().size(); // ✅ Vérifié : `teams` existe bien
            for (Team t : p.getTeams()) {
                t.getMembers().size(); // ✅ Vérifié : `members` existe bien
            }
        }
        return projects;
    }




    @Override
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new DevisNotFoundException("project with ID " + id + " not found"));
    }

    @Override
    public Project saveProject(Project project) {
        project.setId(null);
        return projectRepository.save(project);
    }

    @Override
    public Project updateProject(Long id, Project project) {
        Project existingProject = getProjectById(id);
        existingProject.setName(project.getName());
        existingProject.setDescription(project.getDescription());
        existingProject.setStatus(project.getStatus());
        existingProject.setStartDate(project.getStartDate());
        existingProject.setEndDate(project.getEndDate());
        /*existingProject.setTeam(project.getTeam());
        existingProject.setUser(project.getUser());*/
        return projectRepository.save(existingProject);
    }
    @Override
    public void deleteProject(Long id) {
        projectRepository.deleteById(id);

    }
}
