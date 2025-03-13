package com.workpilot.controller;

import com.workpilot.entity.Team;
import com.workpilot.entity.TeamMember;
import com.workpilot.exception.DevisNotFoundException;
import com.workpilot.repository.TeamRepository;
import com.workpilot.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/teams")
@CrossOrigin(origins = "http://localhost:4200")
public class TeamController {

    @Autowired
    private TeamService teamService;
    @Autowired
    private TeamRepository teamRepository;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<Team>> getAllTeam() {
       return ResponseEntity.ok(teamService.getAllTeam());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Team> getTeamById(@PathVariable Long id) {
        Team team = teamService.getTeamById(id);
        if (team != null) {
            return new ResponseEntity<>(team, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @PostMapping
    public ResponseEntity<Team> createTeam(@RequestBody Team team) {
        Team savedTeam = teamService.saveTeam(team);
        return ResponseEntity.ok(savedTeam);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Team> updateTeam(@PathVariable Long id, @RequestBody Team team) {
        Team updatedTeam = teamService.updateTeam(id, team);
        return ResponseEntity.ok(updatedTeam);
    }


    @DeleteMapping("/{id}")
    public String deleteTeam(@PathVariable("id") Long id) {
        teamService.deleteTeam(id);
        return "Deleted Successfully";
    }

/*    @GetMapping("/{teamId}/members")
    public ResponseEntity<List<TeamMember>> getMembersByTeam(@PathVariable Long teamId) {
        List<TeamMember> members = teamService.getMembersByTeam(teamId);
        return ResponseEntity.ok(members);
    }*/

    @PutMapping("/assigner/{TeamMemberId}/{TeamId}")
    public ResponseEntity<String> assignerTeamMemberTeam(@PathVariable Long teamMemberId, @PathVariable Long teamId) {
        teamService.assignerTeamMemberTeam(teamMemberId, teamId);
        return ResponseEntity.ok("member assignée au team");
    }


}
