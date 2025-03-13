package com.workpilot.controller;

import com.workpilot.entity.Team;
import com.workpilot.entity.TeamMember;
import com.workpilot.service.TeamMemberService;
import com.workpilot.service.TeamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teamMember")
@CrossOrigin(origins = "http://localhost:4200")
public class TeamsMemberController {

    private final TeamMemberService teamMemberService;
    private final TeamService teamService;

    public TeamsMemberController(TeamMemberService teamMemberService, TeamService teamService) {
        this.teamMemberService = teamMemberService;
        this.teamService = teamService;
    }

    @GetMapping
    public ResponseEntity<List<TeamMember>> getAllTeamMembers() {
        List<TeamMember> teamMembers = teamMemberService.getAllTeamMembers();
        return ResponseEntity.ok(teamMembers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamMember> getTeamMemberById(@PathVariable Long id) {
        try {
            TeamMember teamMember = teamMemberService.getTeamMemberById(id);
            return ResponseEntity.ok(teamMember);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<TeamMember> createTeamMember(@RequestBody TeamMember teamMember) {
        if (teamMember.getTeamRole() == null) {
            teamMember.setTeamRole(""); // ✅ Empêche l'erreur NotBlank
        }
        if (teamMember.getNote() == null) {
            teamMember.setNote(""); // ✅ Empêche l'erreur NotBlank
        }

        TeamMember savedMember = teamMemberService.saveTeamMember(teamMember);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedMember);
    }




    @PutMapping("/{id}")
    public ResponseEntity<TeamMember> updateTeamMember(@PathVariable Long id, @RequestBody TeamMember teamMember) {
        TeamMember updatedMember = teamMemberService.updateTeamMember(id, teamMember);
        return ResponseEntity.ok(updatedMember);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeamMember(@PathVariable Long id) {
        teamMemberService.deleteTeamMember(id);
        return ResponseEntity.noContent().build();
    }
}
