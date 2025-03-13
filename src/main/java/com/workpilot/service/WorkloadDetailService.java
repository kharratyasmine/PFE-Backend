package com.workpilot.service;

import com.workpilot.entity.WorkloadDetail;

import java.util.List;

public interface WorkloadDetailService {
    WorkloadDetail createWorkloadDetail(WorkloadDetail workloadDetail);
    WorkloadDetail updateWorkloadDetail(Long id, WorkloadDetail workloadDetail);
    WorkloadDetail getWorkloadDetailById(Long id);
    void deleteWorkloadDetail(Long id);
    List<WorkloadDetail> GetAllWorkloadDetail();
}
