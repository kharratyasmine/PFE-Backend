package com.workpilot.controller;

import com.workpilot.entity.CalendarificResponse;
import com.workpilot.service.PublicHolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/holidays")
public class PublicHolidayController {

    @Autowired
    private PublicHolidayService publicHolidayService;

    @GetMapping("/{year}")
    public List<CalendarificResponse.Holiday> getHolidays(@PathVariable int year) {
        return publicHolidayService.getPublicHolidays(year);
    }
}
