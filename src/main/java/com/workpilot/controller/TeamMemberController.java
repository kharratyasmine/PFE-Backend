package com.workpilot.controller;

import com.workpilot.dto.TeamMemberDTO;
import com.workpilot.service.GestionProject.teamMember.TeamMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teamMembers")
@CrossOrigin(origins = "http://localhost:4200")
public class TeamMemberController {

    @Autowired
    private TeamMemberService teamMemberService;

    @GetMapping
    public List<TeamMemberDTO> getAllTeamMembers() {
        return teamMemberService.getAllTeamMembers();
    }

    @GetMapping("/{id}")
    public TeamMemberDTO getTeamMemberById(@PathVariable Long id) {
        return teamMemberService.getTeamMemberById(id);
    }

    @PostMapping
    public TeamMemberDTO createTeamMember(@RequestBody TeamMemberDTO teamMemberDTO) {
        return teamMemberService.createTeamMember(teamMemberDTO);
    }

    @GetMapping("/project/{projectId}/members")
    public List<TeamMemberDTO> getMembersByProject(@PathVariable Long projectId) {
        return teamMemberService.getMembersByProjectId(projectId);
    }

    // TeamMemberController.java
    @GetMapping("/team/{teamId}")
    public List<TeamMemberDTO> getMembersByTeam(@PathVariable Long teamId) {
        return teamMemberService.getTeamMembersByTeamId(teamId);
    }

    @GetMapping("/team/{teamId}/members")
    public List<TeamMemberDTO> getTeamMembersByTeam(@PathVariable Long teamId) {
        return teamMemberService.getTeamMembersByTeamId(teamId);
    }


    @PutMapping("/{id}")
    public TeamMemberDTO updateTeamMember(@PathVariable Long id, @RequestBody TeamMemberDTO teamMemberDTO) {
        return teamMemberService.updateTeamMember(id, teamMemberDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteTeamMember(@PathVariable Long id) {
        teamMemberService.deleteTeamMember(id);
    }
}