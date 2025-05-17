package com.workpilot.controller.GestionRessources;

import com.workpilot.service.GestionRessources.Holiday.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/holidays")
public class HolidayController {

    @Autowired
    private HolidayService holidayService;

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<Map<String, Object>>> getHolidaysByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(holidayService.getHolidaysByMember(memberId));
    }

    @GetMapping("/check/{memberId}/{date}")
    public ResponseEntity<Boolean> checkHolidayExists(
            @PathVariable Long memberId,
            @PathVariable String date) {
        return ResponseEntity.ok(holidayService.checkHolidayExists(memberId, LocalDate.parse(date)));
    }

    @PostMapping("/simple")
    public ResponseEntity<Void> addSimpleHoliday(@RequestBody Map<String, Object> payload) {
        Long memberId = Long.valueOf(payload.get("memberId").toString());
        String date = payload.get("date").toString();

        holidayService.addSimpleHoliday(memberId, LocalDate.parse(date));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/simple/{memberId}/{date}")
    public ResponseEntity<Void> deleteSimpleHoliday(
            @PathVariable Long memberId,
            @PathVariable String date) {
        holidayService.deleteSimpleHoliday(memberId, LocalDate.parse(date));
        return ResponseEntity.ok().build();
    }
}