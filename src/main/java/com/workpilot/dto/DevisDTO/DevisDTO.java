package com.workpilot.dto.DevisDTO;


import com.workpilot.dto.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DevisDTO {
    private Long id;
    private String reference;
    private String edition;
    private LocalDate creationDate;
    private String author;
    private String status;
    private String proposalValidity;
    private String clientName;
    private String userName;

    private ClientDTO client;
    private UserDTO user;
    private Long projectId; // On ne charge que l'ID du projet
    private ProjectDTO project;
    private List<FinancialDetailDTO> financialDetails;
    private List<WorkloadDetailDTO> workloadDetails;
    private List<InvoicingDetailDTO> invoicingDetails;
    private List<ProposalSummaryDTO> proposalSummary;
    private List<DevisHistoryDTO> devisHistory;
    private List<DistributionDTO> distribution;
    private List<VisaDTO> visa;

    public DevisDTO(Long id, String reference, String author) {
        this.id = id;
        this.reference = reference;
        this.author = author;
    }

}
