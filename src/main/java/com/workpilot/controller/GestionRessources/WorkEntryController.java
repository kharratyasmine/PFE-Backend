package com.workpilot.controller.GestionRessources;

import com.workpilot.entity.ressources.WorkEntry;
import com.workpilot.service.GestionRessources.WorkEntry.WorkEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/work-entries")
public class WorkEntryController {
    @Autowired
    private WorkEntryService workEntryService;

    @GetMapping("/task/{taskId}")
    public List<WorkEntry> getWorkEntriesByTask(@PathVariable Long taskId) {
        return workEntryService.getWorkEntriesByTask(taskId);
    }

    @GetMapping("/member/{memberId}")
    public List<WorkEntry> getWorkEntriesByMember(@PathVariable Long memberId) {
        return workEntryService.getWorkEntriesByMember(memberId);
    }

    @GetMapping("/member/{memberId}/task/{taskId}")
    public List<WorkEntry> getWorkEntriesByMemberAndTask(
            @PathVariable Long memberId,
            @PathVariable Long taskId) {
        return workEntryService.getWorkEntriesByMemberAndTask(memberId, taskId);
    }

    @PostMapping
    public WorkEntry createWorkEntry(@RequestBody WorkEntry workEntry) {
        return workEntryService.createWorkEntry(workEntry);
    }

    @PutMapping("/{id}")
    public WorkEntry updateWorkEntry(
            @PathVariable Long id,
            @RequestBody WorkEntry workEntry) {
        return workEntryService.updateWorkEntry(id, workEntry);
    }

    @DeleteMapping("/{id}")
    public void deleteWorkEntry(@PathVariable Long id) {
        workEntryService.deleteWorkEntry(id);
    }
}