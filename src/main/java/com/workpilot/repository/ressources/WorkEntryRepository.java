package com.workpilot.repository.ressources;

import com.workpilot.entity.ressources.WorkEntry;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface WorkEntryRepository extends JpaRepository<WorkEntry, Long> {
    List<WorkEntry> findByTaskId(Long taskId);

    List<WorkEntry> findByMemberId(Long memberId);

    List<WorkEntry> findByMemberIdAndTaskId(Long memberId, Long taskId);

    void deleteByTaskId(Long id);

    List<WorkEntry> findByMemberIdAndTaskIdAndDate(Long memberId, Long taskId, LocalDate date);

   /* @Modifying
    @Transactional
    @Query("DELETE FROM TaskAssignment a WHERE a.task.id = :taskId")
    void deleteByTaskId(@Param("taskId") Long taskId);*/

    /*@Modifying
    @Transactional
    @Query("DELETE FROM WorkEntry w WHERE w.task.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);*/

}