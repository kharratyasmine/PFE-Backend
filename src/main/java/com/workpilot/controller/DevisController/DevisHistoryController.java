package com.workpilot.controller.DevisController;

import com.workpilot.dto.DevisDTO.DevisHistoryDTO;
import com.workpilot.service.DevisServices.DevisHistory.DevisHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/history")
@CrossOrigin(origins = "http://localhost:4200")
public class DevisHistoryController {

    @Autowired
    private DevisHistoryService devisHistoryService;

    // Endpoint to get all histories for a specific Devis
    @GetMapping("/devis/{devisId}")
    public List<DevisHistoryDTO> getHistoriesByDevisId(@PathVariable Long devisId) {
        return devisHistoryService.getHistoriesByDevisId(devisId);
    }

    // Endpoint to create a new history
    @PostMapping("/devis/{devisId}")
    public DevisHistoryDTO createHistory(@PathVariable Long devisId, @RequestBody DevisHistoryDTO dto) {
        return devisHistoryService.createHistory(devisId, dto);
    }

    @PutMapping("/history/{id}")
    public DevisHistoryDTO updateHistory(@PathVariable Long id, @RequestBody DevisHistoryDTO dto) {
        return devisHistoryService.updateHistory(id, dto);
    }

    @DeleteMapping("/history/{id}")
    public void deleteHistory(@PathVariable Long id) {
        devisHistoryService.deleteHistory(id);
    }
}
