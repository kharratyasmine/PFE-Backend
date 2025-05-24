package com.workpilot.service.GestionRessources.TeamMemberHistory;

import com.workpilot.entity.ressources.TeamMember;
import com.workpilot.entity.ressources.TeamMemberHistory;
import com.workpilot.repository.ressources.TeamMemberHistoryRepository;
import com.workpilot.repository.ressources.TeamMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TeamMemberHistoryServiceImpl implements TeamMemberHistoryService {

    @Autowired
    private TeamMemberHistoryRepository historyRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    public List<TeamMemberHistory> getMemberHistory(Long memberId) {
        TeamMember teamMember = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("TeamMember not found with id: " + memberId));
        return historyRepository.findByTeamMemberOrderByModifiedDateDesc(teamMember);
    }

    public TeamMemberHistory saveHistory(TeamMemberHistory history) {
        if (history.getModifiedDate() == null) {
            history.setModifiedDate(LocalDateTime.now());
        }
        return historyRepository.save(history);
    }

    public void saveHistoryForField(Long memberId, String fieldName, String oldValue, String newValue, String modifiedBy) {
        TeamMemberHistory history = new TeamMemberHistory();
        TeamMember teamMember = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("TeamMember not found with id: " + memberId));
        history.setTeamMember(teamMember);
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setModifiedDate(LocalDateTime.now());
        history.setModifiedBy(modifiedBy);
        historyRepository.save(history);
    }
}