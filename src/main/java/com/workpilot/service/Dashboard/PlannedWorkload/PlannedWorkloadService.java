package com.workpilot.service.Dashboard.PlannedWorkload;

import com.workpilot.dto.Dashboard.PlannedWorkloadDTO;

import java.util.List;

public interface PlannedWorkloadService {
    List<PlannedWorkloadDTO> getByProjectId(Long projectId);
    PlannedWorkloadDTO save(PlannedWorkloadDTO dto);
    void delete(Long id);
}
