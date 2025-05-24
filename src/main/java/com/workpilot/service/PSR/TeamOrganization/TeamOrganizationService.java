package com.workpilot.service.PSR.TeamOrganization;

import com.workpilot.dto.PsrDTO.TeamOrganizationDTO;
import java.util.List;

public interface TeamOrganizationService {
    List<TeamOrganizationDTO> getAll() ;
    List<TeamOrganizationDTO> getTeamByPsrId(Long psrId);
    TeamOrganizationDTO createTeamOrganization(Long psrId, TeamOrganizationDTO dto);
    TeamOrganizationDTO updateTeamOrganization(Long id, TeamOrganizationDTO dto);
    void deleteTeamOrganization(Long id);
    List<TeamOrganizationDTO> getAllProjectMembersForPsr(Long psrId);

    List<TeamOrganizationDTO> getTeamByPsrIdAndWeek(Long psrId, String week);
}
