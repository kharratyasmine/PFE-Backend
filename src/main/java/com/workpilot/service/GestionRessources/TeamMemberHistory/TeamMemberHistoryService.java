package com.workpilot.service.GestionRessources.TeamMemberHistory;

import com.workpilot.entity.ressources.TeamMemberHistory;

import java.util.List;

public interface TeamMemberHistoryService {
    List<TeamMemberHistory> getMemberHistory(Long memberId);

    TeamMemberHistory saveHistory(TeamMemberHistory history);

    void saveHistoryForField(Long memberId, String fieldName, String oldValue, String newValue, String modifiedBy);
}
