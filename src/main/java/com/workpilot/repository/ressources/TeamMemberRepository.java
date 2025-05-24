package com.workpilot.repository.ressources;

import com.workpilot.entity.ressources.Seniority;
import com.workpilot.entity.ressources.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    @Query("SELECT m FROM TeamMember m JOIN m.teams t WHERE t.id = :teamId")
    List<TeamMember> findByTeamId(@Param("teamId") Long teamId);


    @Query("SELECT m FROM TeamMember m JOIN m.teams t JOIN t.projects p WHERE p.id = :projectId")
    List<TeamMember> findMembersByProjectId(@Param("projectId") Long projectId);

    Optional<TeamMember> findByFakeTrueAndRole(Seniority role);
    boolean existsByFakeTrueAndRole(Seniority role);

    Optional<TeamMember> findByRoleAndFake(String role, boolean fake);


}