package com.workpilot.repository.ressources;

import com.workpilot.entity.ressources.PlannedWorkloadMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlannedWorkloadMemberRepository extends JpaRepository<PlannedWorkloadMember, Long> {
    List<PlannedWorkloadMember> findByProjectId(Long projectId);
    void deleteByProjectId(Long projectId);
    List<PlannedWorkloadMember> findByProjectIdAndTeamMemberId(Long projectId, Long teamMemberId);
    List<PlannedWorkloadMember> findByProjectIdAndYear(Long projectId, int year);
    Optional<PlannedWorkloadMember> findByProjectIdAndTeamMemberIdAndYearAndMonth(Long projectId, Long teamMemberId, int year, String month);
    void deleteByProjectIdAndTeamMemberId(Long projectId, Long teamMemberId);

}
