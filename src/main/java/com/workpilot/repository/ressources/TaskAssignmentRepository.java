package com.workpilot.repository.ressources;

import com.workpilot.entity.ressources.TaskAssignment;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {
    List<TaskAssignment> findByTaskId(Long taskId);
    List<TaskAssignment> findByTeamMemberId(Long memberId);

    @Modifying
    @Transactional
    @Query("DELETE FROM TaskAssignment ta WHERE ta.task.id = :taskId")
    void deleteByTaskId(Long taskId);
    Optional<TaskAssignment> findByTaskIdAndTeamMemberId(Long taskId, Long memberId);


}