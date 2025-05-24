package com.workpilot.controller.GestionRessources;

import com.workpilot.entity.ressources.TeamMemberHistory;
import com.workpilot.service.GestionRessources.TeamMemberHistory.TeamMemberHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/teamMembers")
@CrossOrigin(origins = "http://localhost:4200")
public class TeamMemberHistoryController {

    @Autowired
    private TeamMemberHistoryService historyService;

    @GetMapping("/{memberId}/history")
    public ResponseEntity<List<TeamMemberHistory>> getMemberHistory(@PathVariable Long memberId) {
        try {
            List<TeamMemberHistory> history = historyService.getMemberHistory(memberId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/history")
    public ResponseEntity<TeamMemberHistory> saveHistory(@RequestBody TeamMemberHistory history) {
        try {
            TeamMemberHistory savedHistory = historyService.saveHistory(history);
            return ResponseEntity.ok(savedHistory);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}