package com.workpilot.service.DevisServices.ProposalSummary;

import com.workpilot.entity.devis.Devis;
import com.workpilot.entity.devis.WorkloadDetail;
import com.workpilot.repository.devis.DevisRepository;
import com.workpilot.dto.DevisDTO.ProposalSummaryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProposalSummaryServiceImpl implements ProposalSummaryService {

    @Autowired
    private DevisRepository devisRepository;

    @Override
    public ProposalSummaryDTO getProposalSummaryByDevisId(Long devisId) {
        Devis devis = devisRepository.findById(devisId)
                .orElseThrow(() -> new RuntimeException("Devis not found"));

        return ProposalSummaryDTO.builder()
                .customer(
                String.join(", ", devis.getProject().getClient().getSalesManagers())
        )
                .project(devis.getProject().getName())
                .projectType(devis.getProject().getProjectType())
                .proposalValidity(devis.getProposalValidity())
                .estimatedWorkload(
                        devis.getWorkloadDetails().stream()
                                .mapToInt(WorkloadDetail::getEstimatedWorkload)
                                .sum()
                )
                // à transformer si besoin
                .possibleStartDate(devis.getProject().getStartDate())
                .estimatedEndDate(devis.getProject().getEndDate())
                .technicalAspect("À remplir")
                .organizationalAspect("À remplir")
                .commercialAspect("À remplir")
                .qualityAspect("À remplir")
                .build();
    }

}