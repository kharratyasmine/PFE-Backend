package com.workpilot.service.GestionRessources.WorkEntry;

import com.workpilot.entity.ressources.WorkEntry;
import com.workpilot.repository.ressources.WorkEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WorkEntryServiceImpl implements WorkEntryService {

    @Autowired
    private WorkEntryRepository workEntryRepository;

    @Override
    public List<WorkEntry> getWorkEntriesByTask(Long taskId) {
        return workEntryRepository.findByTaskId(taskId);
    }

    @Override
    public List<WorkEntry> getWorkEntriesByMember(Long memberId) {
        return workEntryRepository.findByMemberId(memberId);
    }

    @Override
    public List<WorkEntry> getWorkEntriesByMemberAndTask(Long memberId, Long taskId) {
        return workEntryRepository.findByMemberIdAndTaskId(memberId, taskId);
    }

    @Override
    public WorkEntry createWorkEntry(WorkEntry workEntry) {
        // Vérifier l'existence d'une entrée similaire
        List<WorkEntry> existingEntries = workEntryRepository.findByMemberIdAndTaskIdAndDate(
                workEntry.getMemberId(),
                workEntry.getTaskId(),
                workEntry.getDate()
        );

        if (!existingEntries.isEmpty()) {
            // Mettre à jour l'entrée existante au lieu d'en créer une nouvelle
            WorkEntry existingEntry = existingEntries.get(0);
            existingEntry.setStatus(workEntry.getStatus());
            existingEntry.setComment(workEntry.getComment());
            return workEntryRepository.save(existingEntry);
        }

        return workEntryRepository.save(workEntry);
    }

    @Override
    public WorkEntry updateWorkEntry(Long id, WorkEntry workEntry) {
        workEntry.setId(id);
        return workEntryRepository.save(workEntry);
    }

    @Override
    public void deleteWorkEntry(Long id) {
        workEntryRepository.deleteById(id);
    }

    @Override
    public WorkEntry getWorkEntryById(Long id) {
        return workEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkEntry not found with id: " + id));
    }

}