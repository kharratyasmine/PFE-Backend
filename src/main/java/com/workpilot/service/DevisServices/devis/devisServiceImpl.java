package com.workpilot.service.DevisServices.devis;

import com.workpilot.dto.*;
import com.workpilot.dto.DevisDTO.DevisDTO;
import com.workpilot.dto.DevisDTO.FinancialDetailDTO;
import com.workpilot.dto.DevisDTO.InvoicingDetailDTO;
import com.workpilot.dto.DevisDTO.WorkloadDetailDTO;
import com.workpilot.entity.devis.Devis;
import com.workpilot.entity.devis.FinancialDetail;
import com.workpilot.entity.devis.InvoicingDetail;
import com.workpilot.entity.devis.WorkloadDetail;
import com.workpilot.repository.ClientRepository;
import com.workpilot.repository.ProjectRepository;
import com.workpilot.repository.devis.DevisRepository;
import com.workpilot.service.GestionProject.project.ProjectService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class devisServiceImpl implements DevisService {

    private final DevisRepository devisRepository;
    private final ProjectRepository projectRepository;
    private final ClientRepository clientRepository;
    private final ProjectService projectService;
    public devisServiceImpl(DevisRepository devisRepository,
                            ProjectRepository projectRepository,
                            ClientRepository clientRepository,
                            ProjectService projectService ) {
        this.devisRepository = devisRepository;
        this.projectRepository = projectRepository;
        this.clientRepository = clientRepository;
        this.projectService = projectService;
    }

    @Override
    public List<DevisDTO> getAllDevis() {
        return devisRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DevisDTO> getDevisById(Long id) {
        return devisRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public DevisDTO createDevis(DevisDTO devisDTO) {
        // Vérifie si le projet existe
        if (devisDTO.getProjectId() == null || !projectRepository.existsById(devisDTO.getProjectId())) {
            throw new RuntimeException("Projet avec ID " + devisDTO.getProjectId() + " introuvable.");
        }

        Devis devis = convertToEntity(devisDTO);

        // Associer les détails financiers au devis
        if (devis.getFinancialDetails() != null) {
            devis.getFinancialDetails().forEach(detail -> detail.setDevis(devis));
        }

        // Associer les charges de travail au devis
        if (devis.getWorkloadDetails() != null) {
            devis.getWorkloadDetails().forEach(workload -> workload.setDevis(devis));
        }

        // Associer les détails de facturation au devis
        if (devis.getInvoicingDetails() != null) {
            devis.getInvoicingDetails().forEach(invoice -> invoice.setDevis(devis));
        }

        return convertToDTO(devisRepository.save(devis));
    }

    @Override
    @Transactional
    public DevisDTO updateDevis(Long id, DevisDTO updatedDevisDTO) {
        // Vérifier si le devis existe en base
        Devis existingDevis = devisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Devis avec ID " + id + " introuvable."));

        // Vérifier si le projet fourni existe
        if (updatedDevisDTO.getProjectId() != null) {
            if (!projectRepository.existsById(updatedDevisDTO.getProjectId())) {
                throw new RuntimeException("Projet avec ID " + updatedDevisDTO.getProjectId() + " introuvable.");
            }
            existingDevis.getProject().setId(updatedDevisDTO.getProjectId());
        }

        // Mise à jour des champs simples
        if (updatedDevisDTO.getReference() != null)
            existingDevis.setReference(updatedDevisDTO.getReference());
        if (updatedDevisDTO.getEdition() != null)
            existingDevis.setEdition(updatedDevisDTO.getEdition());
        if (updatedDevisDTO.getCreationDate() != null)
            existingDevis.setCreationDate(updatedDevisDTO.getCreationDate());
        if (updatedDevisDTO.getStatus() != null)
            existingDevis.setStatus(updatedDevisDTO.getStatus());
        if (updatedDevisDTO.getProposalValidity() != null)
            existingDevis.setProposalValidity(updatedDevisDTO.getProposalValidity());

        // Mise à jour des détails financiers
        if (updatedDevisDTO.getFinancialDetails() != null) {
            existingDevis.getFinancialDetails().clear();
            for (FinancialDetailDTO dto : updatedDevisDTO.getFinancialDetails()) {
                FinancialDetail detail = new FinancialDetail();
                detail.setPosition(dto.getPosition());
                detail.setWorkload(dto.getWorkload());
                detail.setDailyCost(dto.getDailyCost());
                detail.setTotalCost(dto.getTotalCost());
                detail.setDevis(existingDevis);
                existingDevis.getFinancialDetails().add(detail);
            }
        }

        // Mise à jour des charges de travail
        if (updatedDevisDTO.getWorkloadDetails() != null) {
            existingDevis.getWorkloadDetails().clear();
            for (WorkloadDetailDTO dto : updatedDevisDTO.getWorkloadDetails()) {
                WorkloadDetail workload = new WorkloadDetail();
                workload.setPeriod(dto.getPeriod());
                workload.setEstimatedWorkload(dto.getEstimatedWorkload());
                workload.setPublicHolidays(dto.getPublicHolidays());
                workload.setDevis(existingDevis);
                existingDevis.getWorkloadDetails().add(workload);
            }
        }

        // Mise à jour des détails de facturation
        if (updatedDevisDTO.getInvoicingDetails() != null) {
            existingDevis.getInvoicingDetails().clear();
            for (InvoicingDetailDTO dto : updatedDevisDTO.getInvoicingDetails()) {
                InvoicingDetail invoice = new InvoicingDetail();
                invoice.setDescription(dto.getDescription());
                invoice.setInvoicingDate(dto.getInvoicingDate());
                invoice.setAmount(dto.getAmount());
                invoice.setDevis(existingDevis);
                existingDevis.getInvoicingDetails().add(invoice);
            }
        }

        return convertToDTO(devisRepository.save(existingDevis));
    }

    @Override
    @Transactional
    public void deleteDevis(Long idDevis) {
        if (!devisRepository.existsById(idDevis)) {
            throw new RuntimeException("Devis avec ID " + idDevis + " introuvable.");
        }
        devisRepository.deleteById(idDevis);
    }

    /* Méthodes de conversion manuelles */

    private DevisDTO convertToDTO(Devis devis) {
        DevisDTO dto = new DevisDTO();

        dto.setId(devis.getId());
        dto.setReference(devis.getReference());
        dto.setEdition(devis.getEdition());
        dto.setCreationDate(devis.getCreationDate());
        dto.setStatus(devis.getStatus());
        dto.setProposalValidity(devis.getProposalValidity());
        dto.setAuthor(devis.getAuthor());

        // Partie projet
        if (devis.getProject() != null) {
            dto.setProjectId(devis.getProject().getId());

            // Convertir tout le projet (client + user + autres)
            ProjectDTO projectDTO = projectService.convertToDTO(devis.getProject());
            dto.setProject(projectDTO);

            // User name séparé
            if (devis.getProject().getUser() != null) {
                String firstname = devis.getProject().getUser().getFirstname();
                String lastname = devis.getProject().getUser().getLastname();
                dto.setUserName(firstname + " " + lastname);
            }

            // Client name (ex: sales managers concaténés)
            if (devis.getProject().getClient() != null) {
                dto.setClientName(String.join(", ", devis.getProject().getClient().getSalesManagers()));
            }
        }

        // Financial details
        if (devis.getFinancialDetails() != null) {
            dto.setFinancialDetails(devis.getFinancialDetails().stream().map(detail ->
                    FinancialDetailDTO.builder()
                            .id(detail.getId())
                            .position(detail.getPosition())
                            .workload(detail.getWorkload())
                            .dailyCost(detail.getDailyCost())
                            .totalCost(detail.getTotalCost())
                            .build()
            ).collect(Collectors.toList()));
        }

        // Workload details
        if (devis.getWorkloadDetails() != null) {
            dto.setWorkloadDetails(devis.getWorkloadDetails().stream().map(workload ->
                    WorkloadDetailDTO.builder()
                            .id(workload.getId())
                            .period(workload.getPeriod())
                            .estimatedWorkload(workload.getEstimatedWorkload())
                            .publicHolidays(workload.getPublicHolidays())
                            .build()
            ).collect(Collectors.toList()));
        }

        // Invoicing details
        if (devis.getInvoicingDetails() != null) {
            dto.setInvoicingDetails(devis.getInvoicingDetails().stream().map(invoice ->
                    InvoicingDetailDTO.builder()
                            .id(invoice.getId())
                            .description(invoice.getDescription())
                            .invoicingDate(invoice.getInvoicingDate())
                            .amount(invoice.getAmount())
                            .build()
            ).collect(Collectors.toList()));
        }

        return dto;
    }



    private Devis convertToEntity(DevisDTO dto) {
        Devis devis = new Devis();
// champs simples
        devis.setReference(dto.getReference());
        devis.setEdition(dto.getEdition());
        devis.setCreationDate(dto.getCreationDate());
        devis.setStatus(dto.getStatus());
        devis.setProposalValidity(dto.getProposalValidity());
        devis.setAuthor(dto.getAuthor());

// association projet
        projectRepository.findById(dto.getProjectId()).ifPresent(devis::setProject);

// détails financiers
        if (dto.getFinancialDetails() != null) {
            devis.setFinancialDetails(dto.getFinancialDetails().stream().map(detail ->
                    new FinancialDetail(null, detail.getPosition(), detail.getWorkload(),
                            detail.getDailyCost(), detail.getTotalCost(), devis)
            ).collect(Collectors.toList()));
        }

// charges de travail
        if (dto.getWorkloadDetails() != null) {
            devis.setWorkloadDetails(dto.getWorkloadDetails().stream().map(detail ->
                    new WorkloadDetail(null, detail.getPeriod(), detail.getEstimatedWorkload(),
                            detail.getPublicHolidays(), devis)
            ).collect(Collectors.toList()));
        }

// facturation
        if (dto.getInvoicingDetails() != null) {
            devis.setInvoicingDetails(dto.getInvoicingDetails().stream().map(detail ->
                    new InvoicingDetail(null, detail.getDescription(), detail.getInvoicingDate(),
                            detail.getAmount(), devis)
            ).collect(Collectors.toList()));
        }

        return devis;
    }
    @Override
    public List<DevisDTO> getDevisByProjectId(Long projectId) {
        return devisRepository.findByProjectId(projectId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

}
