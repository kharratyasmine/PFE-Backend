package com.workpilot.repository.Psr;

import com.workpilot.entity.PSR.Psr;
import com.workpilot.entity.PSR.TaskTracker;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskTrackerRepository extends JpaRepository<TaskTracker, Long> {
    List<TaskTracker> findByPsr(Psr psr);

    @Modifying
    @Transactional
    @Query("DELETE FROM TaskTracker t WHERE t.psr.id IN (SELECT p.id FROM Psr p WHERE p.project.id = :projectId)")
    void deleteByProjectId(@Param("projectId") Long projectId);
}
