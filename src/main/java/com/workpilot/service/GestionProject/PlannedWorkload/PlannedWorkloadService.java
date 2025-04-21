package com.workpilot.service.GestionProject.PlannedWorkload;

import com.workpilot.dto.PlannedWorkloadDTO;
import com.workpilot.entity.ressources.PlannedWorkload;

import java.util.List;

public interface PlannedWorkloadService {
    List<PlannedWorkloadDTO> getByProject(Long projectId);
    PlannedWorkloadDTO save(PlannedWorkloadDTO dto);
    void delete(Long id);
    PlannedWorkloadDTO update(Long id, PlannedWorkloadDTO dto);
        


}
