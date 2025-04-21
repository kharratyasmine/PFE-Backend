package com.workpilot.repository;

import com.workpilot.entity.ressources.TaskAssignment;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {
    List<TaskAssignment> findByTaskId(Long taskId);
    List<TaskAssignment> findByTeamMemberId(Long memberId);

    @Modifying
    @Transactional
    @Query("DELETE FROM TaskAssignment ta WHERE ta.task.id = :taskId")
    void deleteByTaskId(Long taskId);
}