package com.workpilot.repository.Dashboard;

import com.workpilot.entity.Dashboard.PlannedWorkload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PlannedWorkloadRepository extends JpaRepository<PlannedWorkload, Long> {
    List<PlannedWorkload> findByProjectId(Long projectId);
}