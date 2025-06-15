package com.workpilot.service.PSR.weeklyReport;

import com.workpilot.dto.PsrDTO.WeeklyReportDTO;

import java.util.List;

public interface WeeklyReportService {
    void generateReportFromPsr(Long psrId);

    List<WeeklyReportDTO> getReportsByMonth(String month, int year);

    List<WeeklyReportDTO> getReportsByMonthAndPsr(String month, int year, Long psrId);
}
