package com.workpilot.repository.Psr;

import com.workpilot.entity.PSR.Psr;
import com.workpilot.entity.PSR.WeeklyReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Long> {
    List<WeeklyReport> findByPsrId(Long psrId);

    void deleteByPsr(Psr psr);
}
