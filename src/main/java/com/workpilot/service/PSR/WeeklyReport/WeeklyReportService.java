package com.workpilot.service.PSR.WeeklyReport;

import com.workpilot.dto.PsrDTO.WeeklyReportDTO;

import java.util.List;

public interface WeeklyReportService {
    WeeklyReportDTO save(WeeklyReportDTO dto);
    List<WeeklyReportDTO> findByPsrId(Long psrId);
    void delete(Long id);
    void generateWeeklyReportsByPsr(Long psrId);
}
