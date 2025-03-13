package com.workpilot.repository;

import com.workpilot.entity.Team;
import com.workpilot.entity.TeamMember;
import com.workpilot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List <TeamMember>findByTeamId(Long teamId);

}
