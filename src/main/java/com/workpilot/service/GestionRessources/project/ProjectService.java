package com.workpilot.service.GestionRessources.project;

import com.workpilot.dto.GestionRessources.ProjectDTO;
import com.workpilot.dto.GestionRessources.TeamAllocationDTO;
import com.workpilot.dto.GestionRessources.TeamMemberAllocationDTO;
import com.workpilot.dto.GestionRessources.TeamMemberDTO;
import com.workpilot.entity.ressources.Project;
import java.util.List;

public interface ProjectService {
    Project  createProject(ProjectDTO dto);
    ProjectDTO  updateProject(Long id, ProjectDTO dto);
    Project getProjectById(Long id);
    List<ProjectDTO> GetAllProject();
    void assignTeamToProject(Long projectId, Long teamId);
    List<TeamMemberAllocationDTO> getAllocationsByProjectId(Long projectId);
    List<TeamAllocationDTO> getTeamAllocationsByProjectId(Long projectId);
    List<TeamMemberDTO> getMembersByProject(Long projectId);

    void deleteProjectById(Long projectId);

    ProjectDTO convertToDTO(Project project);
    void removeTeamFromProject(Long projectId, Long teamId);
}
