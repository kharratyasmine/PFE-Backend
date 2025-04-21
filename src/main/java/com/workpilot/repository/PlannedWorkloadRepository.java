package com.workpilot.repository;


import com.workpilot.entity.ressources.PlannedWorkload;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlannedWorkloadRepository extends JpaRepository<PlannedWorkload, Long> {
    List<PlannedWorkload> findByProjectId(Long projectId);
}