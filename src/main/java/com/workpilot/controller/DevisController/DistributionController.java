package com.workpilot.controller.DevisController;

import com.workpilot.dto.DevisDTO.DistributionDTO;


import com.workpilot.service.DevisServices.DevisDistribution.DistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/distribution")
@CrossOrigin(origins = "http://localhost:4200")
public class DistributionController {
    @Autowired
    private DistributionService distributionService;

    // Get all distribution info by DevisId
    @GetMapping("/devis/{devisId}")
    public List<DistributionDTO> getDistributionsByDevisId(@PathVariable Long devisId) {
        return distributionService.getDistributionsByDevisId(devisId);
    }

    // Add a new Distribution
    @PostMapping("/devis/{devisId}")
    public DistributionDTO addDistribution(@PathVariable Long devisId, @RequestBody DistributionDTO distributionDTO) {
        return distributionService.addDistribution(devisId, distributionDTO);
    }

    // Update a specific Distribution
    @PutMapping("/{distributionId}")
    public DistributionDTO updateDistribution(@PathVariable Long distributionId, @RequestBody DistributionDTO distributionDTO) {
        return distributionService.updateDistribution(distributionId, distributionDTO);
    }
}
