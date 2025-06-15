package com.workpilot.service.GestionRessources.WorkEntry;



import com.workpilot.entity.ressources.WorkEntry;

import java.util.List;

public interface WorkEntryService {
    List<WorkEntry> getWorkEntriesByTask(Long taskId);
    List<WorkEntry> getWorkEntriesByMember(Long memberId);
    List<WorkEntry> getWorkEntriesByMemberAndTask(Long memberId, Long taskId);
    WorkEntry createWorkEntry(WorkEntry workEntry);
    WorkEntry updateWorkEntry(Long id, WorkEntry workEntry);
    void deleteWorkEntry(Long id);

    WorkEntry getWorkEntryById(Long id);
}