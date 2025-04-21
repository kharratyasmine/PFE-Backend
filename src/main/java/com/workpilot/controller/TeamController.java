package com.workpilot.controller;

import com.workpilot.dto.TeamDTO;
import com.workpilot.dto.TeamMemberDTO;
import com.workpilot.service.GestionProject.team.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams")
@CrossOrigin(origins = "http://localhost:4200")
public class TeamController {

    @Autowired
    private TeamService teamService;

    // Récupérer toutes les équipes
    @GetMapping
    public List<TeamDTO> getAllTeams() {
        return teamService.getAllTeams();
    }

    // Récupérer une équipe par son ID
    @GetMapping("/{id}")
    public TeamDTO getTeamById(@PathVariable Long id) {
        return teamService.getTeamById(id);
    }

    // Créer une nouvelle équipe
    @PostMapping
    public TeamDTO createTeam(@RequestBody TeamDTO teamDTO) {
        return teamService.createTeam(teamDTO);
    }

    // Mettre à jour une équipe existante
    @PutMapping("/{id}")
    public TeamDTO updateTeam(@PathVariable Long id, @RequestBody TeamDTO teamDTO) {
        return teamService.updateTeam(id, teamDTO);
    }

    // Supprimer une équipe
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id); // suppression définitive
        return ResponseEntity.ok("Team supprimée");
    }


    // Ajouter un membre à une équipe
    @PutMapping("/{teamId}/add-member/{memberId}")
    public ResponseEntity<Void> addMemberToTeam(@PathVariable Long teamId, @PathVariable Long memberId) {
        teamService.addMemberToTeam(teamId, memberId);
        return ResponseEntity.ok().build();
    }


    // Associer une équipe à un projet
    @PostMapping("/{teamId}/add-project/{projectId}")
    public TeamDTO addProjectToTeam(@PathVariable Long teamId, @PathVariable Long projectId) {
        return teamService.addProjectToTeam(teamId, projectId);
    }

    // Retirer un membre d'une équipe
    @DeleteMapping("/{teamId}/remove-member/{memberId}")
    public ResponseEntity<Void> removeMemberFromTeam(
            @PathVariable Long teamId,
            @PathVariable Long memberId) {
        teamService.removeMemberFromTeam(teamId, memberId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{teamId}/members")
    public List<TeamMemberDTO> getMembersOfTeam(@PathVariable Long teamId) {
        return teamService.getMembersOfTeam(teamId);
    }

    @GetMapping("/{teamId}/available-members")
    public List<TeamMemberDTO> getAvailableMembers(@PathVariable Long teamId) {
        return teamService.getAvailableMembers(teamId);
    }



}