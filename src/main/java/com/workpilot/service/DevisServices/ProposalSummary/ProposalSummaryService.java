package com.workpilot.service.DevisServices.ProposalSummary;

import com.workpilot.dto.DevisDTO.ProposalSummaryDTO;

public interface ProposalSummaryService {
    ProposalSummaryDTO getProposalSummaryByDevisId(Long devisId);
}

