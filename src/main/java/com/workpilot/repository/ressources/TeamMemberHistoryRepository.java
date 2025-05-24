package com.workpilot.repository.ressources;

import com.workpilot.entity.ressources.TeamMember;
import com.workpilot.entity.ressources.TeamMemberHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TeamMemberHistoryRepository extends JpaRepository<TeamMemberHistory, Long> {
    List<TeamMemberHistory> findByTeamMemberOrderByModifiedDateDesc(TeamMember teamMember);
}