package com.workpilot.controller.PSRController;

import com.workpilot.dto.PsrDTO.TeamOrganizationDTO;
import com.workpilot.service.PSR.TeamOrganization.TeamOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class TeamOrganizationController {

    @Autowired
    private TeamOrganizationService teamOrganizationService;

    @GetMapping("/psr/{psrId}/team-Organization")
    public ResponseEntity<List<TeamOrganizationDTO>> getTeamOrganizationByPsr(@PathVariable Long psrId) {
        List<TeamOrganizationDTO> teamList = teamOrganizationService.getTeamByPsrId(psrId);
        return ResponseEntity.ok(teamList);
    }

    @GetMapping("/teamOrganization/psr/{psrId}/week/{week}")
    public ResponseEntity<List<TeamOrganizationDTO>> getTeamOrganizationByWeek(
            @PathVariable Long psrId,
            @PathVariable String week) {
        List<TeamOrganizationDTO> teamList = teamOrganizationService.getTeamByPsrIdAndWeek(psrId, week);
        return ResponseEntity.ok(teamList);
    }

    // Endpoint to create a new history
    @PostMapping("/psr/{psrId}")
    public TeamOrganizationDTO createTeamOrganization(@PathVariable Long psrId, @RequestBody TeamOrganizationDTO dto) {
        return teamOrganizationService.createTeamOrganization(psrId, dto);
    }

    @PutMapping("/{id}")
    public TeamOrganizationDTO updateTeamOrganization(@PathVariable Long id, @RequestBody TeamOrganizationDTO dto) {
        return teamOrganizationService.updateTeamOrganization(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteTeamOrganization(@PathVariable Long id) {
        teamOrganizationService.deleteTeamOrganization(id);
    }

    @GetMapping("/psr/{psrId}/project-members")
    public ResponseEntity<List<TeamOrganizationDTO>> getAllProjectMembers(@PathVariable Long psrId) {
        return ResponseEntity.ok(teamOrganizationService.getAllProjectMembersForPsr(psrId));
    }


}