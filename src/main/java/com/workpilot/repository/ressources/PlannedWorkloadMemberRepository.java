package com.workpilot.repository.ressources;

import com.workpilot.entity.ressources.PlannedWorkloadMember;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlannedWorkloadMemberRepository extends JpaRepository<PlannedWorkloadMember, Long> {
    List<PlannedWorkloadMember> findByProjectId(Long projectId);
    List<PlannedWorkloadMember> findByProjectIdAndTeamMemberId(Long projectId, Long teamMemberId);
    List<PlannedWorkloadMember> findByProjectIdAndYear(Long projectId, int year);
    Optional<PlannedWorkloadMember> findByProjectIdAndTeamMemberIdAndYearAndMonth(Long projectId, Long teamMemberId, int year, String month);
    void deleteByProjectIdAndTeamMemberId(Long projectId, Long teamMemberId);


    @Modifying
    @Transactional
    @Query("DELETE FROM PlannedWorkloadMember p WHERE p.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);
}
