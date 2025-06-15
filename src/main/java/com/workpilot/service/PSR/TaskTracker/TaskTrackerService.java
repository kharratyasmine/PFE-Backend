package com.workpilot.service.PSR.TaskTracker;

import com.workpilot.dto.PsrDTO.TaskTrackerDTO;
import com.workpilot.entity.PSR.Psr;

import java.util.List;

public interface TaskTrackerService {
    TaskTrackerDTO save(TaskTrackerDTO dto);
    List<TaskTrackerDTO> getByPsr(Long psrId);

    void synchronizeTaskTrackers(Psr psr);
    void delete(Long id);
    void generateFromAssignments(Long psrId);
    TaskTrackerDTO update(Long id, TaskTrackerDTO dto);
}
