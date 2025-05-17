package com.workpilot.controller;

import com.workpilot.entity.CalendarificResponse;
import com.workpilot.service.PublicHolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/holidays")
public class PublicHolidayController {

    @Autowired
    private PublicHolidayService publicHolidayService;

    // ✅ Endpoint principal — jours fériés complets pour une année
    @GetMapping("/{year}")
    public List<CalendarificResponse.HolidayWrapper> getHolidays(@PathVariable int year) {
        return publicHolidayService.getPublicHolidays(year);
    }

    // ✅ Endpoint simplifié — juste les noms + dates des jours fériés officiels
    @GetMapping("/{year}/simple")
    public List<CalendarificResponse.Holiday> getSimpleHolidays(@PathVariable int year) {
        return publicHolidayService.getSimplifiedHolidays(year);
    }

    // ✅ Jours fériés combinés (officiels uniquement) en LocalDate
    @GetMapping("/{year}/dates")
    public Set<LocalDate> getHolidayDates(@PathVariable int year) {
        return publicHolidayService.getAllCombinedHolidays(year);
    }

    // ✅ Jours fériés entre deux dates (utile pour planning)
    @GetMapping("/between")
    public Set<LocalDate> getBetween(
            @RequestParam("start") LocalDate start,
            @RequestParam("end") LocalDate end) {
        return publicHolidayService.getAllCombinedHolidaysBetween(start, end);
    }
}
