package com.workpilot.repository.Psr;

import com.workpilot.entity.PSR.WeeklyReport;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Long> {
    List<WeeklyReport> findByMonthAndYear(String month, int year);

    List<WeeklyReport> findByMonthAndYearAndPsrId(String month, int year, Long psrId);

    @Modifying
    @Transactional
    @Query("DELETE FROM WeeklyReport w WHERE w.psr.id IN (SELECT p.id FROM Psr p WHERE p.project.id = :projectId)")
    void deleteByProjectId(@Param("projectId") Long projectId);


    @Modifying
    @Transactional
    @Query("DELETE FROM WeeklyReport w WHERE w.psr.id = :psrId")
    void deleteByPsrId(Long psrId);

    Optional<WeeklyReport> findByWeekNumberAndYearAndPsrId(int weekNumber, int year, Long psrId);

}
