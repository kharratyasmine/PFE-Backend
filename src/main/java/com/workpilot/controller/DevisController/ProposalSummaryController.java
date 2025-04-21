package com.workpilot.controller.DevisController;

import com.workpilot.dto.DevisDTO.ProposalSummaryDTO;
import com.workpilot.service.DevisServices.ProposalSummary.ProposalSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/proposalSummary")
@CrossOrigin(origins = "http://localhost:4200")
public class ProposalSummaryController {

    @Autowired
    private ProposalSummaryService summaryService;

    @GetMapping("/devis/{devisId}")
    public ProposalSummaryDTO getProposalSummary(@PathVariable Long devisId) {
        return summaryService.getProposalSummaryByDevisId(devisId);
    }
}
