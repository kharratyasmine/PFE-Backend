package com.workpilot.repository;

import com.workpilot.entity.ressources.TeamMember;
import com.workpilot.entity.ressources.TeamMemberAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    @Query("SELECT m FROM TeamMember m JOIN m.teams t WHERE t.id = :teamId")
    List<TeamMember> findByTeamId(@Param("teamId") Long teamId);


    @Query("SELECT m FROM TeamMember m JOIN m.teams t JOIN t.projects p WHERE p.id = :projectId")
    List<TeamMember> findMembersByProjectId(@Param("projectId") Long projectId);

}