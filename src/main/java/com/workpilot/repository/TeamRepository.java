package com.workpilot.repository;

import com.workpilot.entity.ressources.Team;
import com.workpilot.entity.ressources.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    }