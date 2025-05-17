package com.workpilot.repository.ressources;

import com.workpilot.entity.ressources.ProjectTask;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectTaskRepository extends JpaRepository<ProjectTask, Long> {

    List<ProjectTask> findByProject_Id(Long projectId);
/*
    @Query("SELECT DISTINCT t FROM ProjectTask t LEFT JOIN FETCH t.project p LEFT JOIN FETCH t.assignedMembers m")
    List<ProjectTask> findAllWithProjectAndMember();

    @Modifying
    @Transactional
    @Query("DELETE FROM ProjectTask t WHERE t.assignedTo.id = :teamMemberId")
    void deleteByTeamMemberId(@Param("teamMemberId") Long teamMemberId);
*/

    @Transactional
    @Modifying
    @Query("DELETE FROM ProjectTask t WHERE t.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);
}
