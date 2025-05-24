package com.workpilot.repository.Psr;

import com.workpilot.entity.PSR.Psr;
import com.workpilot.entity.PSR.TaskTracker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskTrackerRepository extends JpaRepository<TaskTracker, Long> {
    List<TaskTracker> findByPsr(Psr psr);

}
