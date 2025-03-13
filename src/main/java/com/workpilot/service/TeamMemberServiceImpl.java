package com.workpilot.service;

import com.workpilot.entity.TeamMember;
import com.workpilot.exception.DevisNotFoundException;
import com.workpilot.repository.TeamMemberRepository;
import com.workpilot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamMemberServiceImpl implements TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;

    @Autowired
    private UserRepository userRepository;

    public TeamMemberServiceImpl(TeamMemberRepository teamMemberRepository) {
        this.teamMemberRepository = teamMemberRepository;
    }


    @Override
    public List<TeamMember> getAllTeamMembers() {
        return teamMemberRepository.findAll();
    }

    public List<TeamMember> getMembersByTeam(Long teamId) {
        return teamMemberRepository.findByTeamId(teamId);
    }

    @Override
    public TeamMember getTeamMemberById(Long id) {
        return teamMemberRepository.findById(id)
                .orElseThrow(() -> new DevisNotFoundException("equipe with ID " + id + " not found"));
    }

    @Override
    public TeamMember saveTeamMember(TeamMember teamMember) {
        if (teamMember.getTeam() != null && teamMember.getTeam().getId() == null) {
            teamMember.setTeam(null);
        }
        return teamMemberRepository.save(teamMember);
    }


    @Override
    public TeamMember updateTeamMember(Long id, TeamMember teamMember) {
        TeamMember existingMember = getTeamMemberById(id);
        existingMember.setTeam(teamMember.getTeam());
        return teamMemberRepository.save(existingMember);
    }
    @Override
    public void deleteTeamMember(Long id) {
        teamMemberRepository.deleteById(id);
    }


    @Override
    public TeamMember addMemberToTeam(Long userId, Long teamId, String role, double allocation) {
        return null;
    }
}
