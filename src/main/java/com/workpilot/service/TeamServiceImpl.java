package com.workpilot.service;

import com.workpilot.entity.Team;
import com.workpilot.entity.TeamMember;
import com.workpilot.exception.DevisNotFoundException;
import com.workpilot.exception.TeamNotFoundException;
import com.workpilot.repository.TeamMemberRepository;
import com.workpilot.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.validation.Valid;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TeamServiceImpl implements TeamService {

    @Autowired
    private final TeamRepository teamRepository;
    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    public TeamServiceImpl(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    public List<Team> getAllTeam() {
        return teamRepository.findAll();
    }

    // ✅ Récupérer une équipe par ID avec gestion des erreurs
    @Override
    public Team getTeamById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Équipe introuvable"
                ));
    }

    @Override
    public Team saveTeam(Team team) {
        return teamRepository.save(team);
    }

    @Override
    public Team updateTeam(Long id, Team team) {
        Team existingTeam = teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        existingTeam.setName(team.getName());

        // 🔥 Mettre à jour les membres
        List<TeamMember> updatedMembers = team.getMembers().stream()
                .map(member -> teamMemberRepository.findById(member.getId())
                        .orElseThrow(() -> new RuntimeException("Member not found")))
                .collect(Collectors.toList());

        existingTeam.setMembers(updatedMembers);
        return teamRepository.save(existingTeam);
    }


    @Override
    public void deleteTeam(Long id) {
        teamRepository.deleteById(id);
    }

/*
    // ✅ Récupérer tous les membres
    @Override
    public List<TeamMember> getAllMembers() {
        return teamMemberRepository.findAll();
    }

    @Override
    public List<TeamMember> getMembersByTeam(Long teamId) {
        Team team = getTeamById(teamId); // Vérifie si l'équipe existe
        return team.getMembers(); // Retourne la liste des membres de cette équipe

    }*/
    @Override
    public void assignerTeamMemberTeam(Long TeamMemberId, Long TeamId) {
        TeamMember teamMember = teamMemberRepository.findById(TeamMemberId).orElseThrow(() -> new RuntimeException("Team Member non trouvée"));
        Team team = teamRepository.findById(TeamId).orElseThrow(() -> new RuntimeException("team non trouvée"));

        teamMember.setTeam(team);
        teamMemberRepository.save(teamMember);
    }
}