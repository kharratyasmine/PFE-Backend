package com.workpilot.repository;
import com.workpilot.entity.ProjectTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectTaskRepository extends JpaRepository<ProjectTask, Long> {
}
