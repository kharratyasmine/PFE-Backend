package com.workpilot.service.GestionRessources.Holiday;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface HolidayService {

    List<Map<String, Object>> getHolidaysByMember(Long memberId);

    boolean checkHolidayExists(Long memberId, LocalDate date);

    void addSimpleHoliday(Long memberId, LocalDate date);

    void deleteSimpleHoliday(Long memberId, LocalDate date);
}