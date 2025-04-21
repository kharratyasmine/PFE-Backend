package com.workpilot.service.DevisServices.WorkloadDetail;

import com.workpilot.dto.DevisDTO.WorkloadDetailDTO;
import com.workpilot.entity.devis.WorkloadDetail;

import java.util.List;

public interface WorkloadDetailService {
    WorkloadDetail createWorkloadDetail(WorkloadDetail workloadDetail);
    WorkloadDetail updateWorkloadDetail(Long id, WorkloadDetail workloadDetail);
    WorkloadDetail getWorkloadDetailById(Long id);
    void deleteWorkloadDetail(Long id);
    List<WorkloadDetail> GetAllWorkloadDetail();
    List<WorkloadDetail> getByDevisId(Long devisId);
    List<WorkloadDetailDTO> generateFromDemandes(Long devisId);
    List<WorkloadDetail> saveAll(List<WorkloadDetail> details);

}
