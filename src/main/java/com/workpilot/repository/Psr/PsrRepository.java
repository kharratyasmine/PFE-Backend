package com.workpilot.repository.Psr;

import com.workpilot.entity.PSR.Psr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PsrRepository extends JpaRepository<Psr, Long> {

    List<Psr> findByProjectId(Long projectId);
    boolean existsByProjectIdAndWeekAndReportYear(Long projectId, String week, int reportYear);
    List<Psr> findByProjectIdAndWeekBetween(Long projectId, String startWeek, String endWeek);
    List<Psr> findByProjectIdAndWeekLessThanEqual(Long projectId, String week);
    List<Psr> findByProjectIdAndWeekAndReportYear(Long projectId, String week, int reportYear);
    void deleteByProjectId(Long projectId);
   // List<Psr> findByProjectIdAndReportYear(Long projectId, int year);
}