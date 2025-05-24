package com.workpilot.repository.Psr;

import com.workpilot.entity.PSR.TeamOrganization;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

@Repository
public interface TeamOrganizationRepository extends JpaRepository<TeamOrganization, Long> {
    List<TeamOrganization> findByPsrId(Long psrId);
    boolean existsByPsrIdAndInitialAndFullName(Long psrId, String initial, String fullName);
    List<TeamOrganization> findByPsrProjectId(Long projectId);

    @Query("SELECT t FROM TeamOrganization t WHERE t.psr.id = :psrId AND t.week = :week")
    List<TeamOrganization> findByPsrIdAndWeek(@Param("psrId") Long psrId, @Param("week") String week);

    @Modifying
    @Transactional
    @Query("DELETE FROM TeamOrganization t WHERE t.psr.project.id = :projectId")
    void deleteByPsrProjectId(@Param("projectId") Long projectId);


}
