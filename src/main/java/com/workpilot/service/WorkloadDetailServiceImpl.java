package com.workpilot.service;

import com.workpilot.entity.*;
import com.workpilot.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkloadDetailServiceImpl implements WorkloadDetailService {

    @Autowired
    private WorkloadDetailRepository workloadDetailRepository;

    @Override
    public WorkloadDetail createWorkloadDetail(WorkloadDetail workloadDetail) {
        return workloadDetailRepository.save(workloadDetail);
    }

    @Override
    public WorkloadDetail updateWorkloadDetail(Long id, WorkloadDetail updatedDetail) {
        WorkloadDetail existingDetail = workloadDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkloadDetail avec ID " + id + " introuvable."));

        if (updatedDetail.getPeriod() != null) existingDetail.setPeriod(updatedDetail.getPeriod());
        if (updatedDetail.getEstimatedWorkload() != null) existingDetail.setEstimatedWorkload(updatedDetail.getEstimatedWorkload());
        if (updatedDetail.getPublicHolidays() != null) existingDetail.setPublicHolidays(updatedDetail.getPublicHolidays());
        if (updatedDetail.getActualWorkload() != null) existingDetail.setActualWorkload(updatedDetail.getActualWorkload());

        return workloadDetailRepository.save(existingDetail);
    }

    @Override
    public WorkloadDetail getWorkloadDetailById(Long id) {
        return workloadDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkloadDetail avec ID " + id + " introuvable."));
    }

    @Override
    public void deleteWorkloadDetail(Long id) {
        workloadDetailRepository.deleteById(id);
    }

    @Override
    public List<WorkloadDetail> GetAllWorkloadDetail() {
        return workloadDetailRepository.findAll();
    }



}
