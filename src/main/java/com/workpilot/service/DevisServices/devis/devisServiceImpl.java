package com.workpilot.service.DevisServices.devis;

import com.workpilot.dto.DevisDTO.DevisDTO;
import com.workpilot.dto.DevisDTO.FinancialDetailDTO;
import com.workpilot.dto.DevisDTO.InvoicingDetailDTO;
import com.workpilot.dto.DevisDTO.WorkloadDetailDTO;
import com.workpilot.dto.GestionRessources.ProjectDTO;
import com.workpilot.entity.devis.Devis;
import com.workpilot.entity.devis.FinancialDetail;
import com.workpilot.entity.devis.InvoicingDetail;
import com.workpilot.entity.devis.WorkloadDetail;
import com.workpilot.entity.ressources.Demande;
import com.workpilot.repository.ressources.ClientRepository;
import com.workpilot.repository.ressources.DemandeRepository;
import com.workpilot.repository.ressources.ProjectRepository;
import com.workpilot.repository.devis.DevisRepository;
import com.workpilot.service.GestionRessources.project.ProjectService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class devisServiceImpl implements DevisService {

    private final DevisRepository devisRepository;
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;
    private final DemandeRepository demandeRepository;

    public devisServiceImpl(DevisRepository devisRepository,
                            ProjectRepository projectRepository,
                            ProjectService projectService ,
                            DemandeRepository demandeRepository) {
        this.devisRepository = devisRepository;
        this.projectRepository = projectRepository;

        this.projectService = projectService;
        this.demandeRepository = demandeRepository;
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

        // Vérifie si la demande existe
        if (devisDTO.getDemandeId() == null || !demandeRepository.existsById(devisDTO.getDemandeId())) {
            throw new RuntimeException("Demande avec ID " + devisDTO.getDemandeId() + " introuvable.");
        }

        Devis devis = convertToEntity(devisDTO);
        return convertToDTO(devisRepository.save(devis));
    }

    @Override
    @Transactional
    public DevisDTO updateDevis(Long id, DevisDTO updatedDevisDTO) {
        Devis existingDevis = devisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Devis avec ID " + id + " introuvable."));

        // Vérifier si le projet fourni existe
        if (updatedDevisDTO.getProjectId() != null) {
            if (!projectRepository.existsById(updatedDevisDTO.getProjectId())) {
                throw new RuntimeException("Projet avec ID " + updatedDevisDTO.getProjectId() + " introuvable.");
            }
            existingDevis.getProject().setId(updatedDevisDTO.getProjectId());
        }

        // ✅ Mise à jour de la demande liée (si fournie dans le DTO)
        if (updatedDevisDTO.getDemandeId() != null) {
            Demande demande = demandeRepository.findById(updatedDevisDTO.getDemandeId())
                    .orElseThrow(() -> new RuntimeException("Demande avec ID " + updatedDevisDTO.getDemandeId() + " introuvable."));
            existingDevis.setDemande(demande);
        } else {
            throw new RuntimeException("Le champ demandeId est obligatoire pour mettre à jour un devis.");
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
    private DevisDTO convertToDTO(Devis devis) {
        DevisDTO dto = new DevisDTO();

        dto.setId(devis.getId());
        dto.setReference(devis.getReference());
        dto.setEdition(devis.getEdition());
        dto.setCreationDate(devis.getCreationDate());
        dto.setStatus(devis.getStatus());
        dto.setProposalValidity(devis.getProposalValidity());
        dto.setAuthor(devis.getAuthor());
        // Partie demande
        if (devis.getDemande() != null) {
            dto.setDemandeId(devis.getDemande().getId());
        }


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
                            .devisId(detail.getDevis() != null ? detail.getDevis().getId() : null)
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
                            .publicHolidayDates(workload.getPublicHolidayDates())
                            .numberOfResources(workload.getNumberOfResources())
                            .totalEstimatedWorkload(workload.getTotalEstimatedWorkload())
                            .totalWorkload(workload.getTotalWorkload())
                            .note(workload.getNote())
                            .devisId(workload.getDevis() != null ? workload.getDevis().getId() : null)
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
                            .devisId(invoice.getDevis() != null ? invoice.getDevis().getId() : null)
                            .build()
            ).collect(Collectors.toList()));
        }

        return dto;
    }
    private Devis convertToEntity(DevisDTO dto) {
        Devis devis = new Devis();

        devis.setReference(dto.getReference());
        devis.setEdition(dto.getEdition());
        devis.setCreationDate(dto.getCreationDate());
        devis.setStatus(dto.getStatus());
        devis.setProposalValidity(dto.getProposalValidity());
        devis.setAuthor(dto.getAuthor());

        // Project
        projectRepository.findById(dto.getProjectId()).ifPresent(devis::setProject);

        // Demande
        Demande demande = demandeRepository.findById(dto.getDemandeId())
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));
        devis.setDemande(demande);

        // Financial details
        if (dto.getFinancialDetails() != null) {
            devis.setFinancialDetails(dto.getFinancialDetails().stream().map(detail ->
                    new FinancialDetail(null, detail.getPosition(), detail.getWorkload(),
                            detail.getDailyCost(), detail.getTotalCost(), demande, devis)
            ).collect(Collectors.toList()));
        }

        // Workload details
        devis.setWorkloadDetails(dto.getWorkloadDetails().stream().map(detail ->
                WorkloadDetail.builder()
                        .period(detail.getPeriod())
                        .estimatedWorkload(detail.getEstimatedWorkload())
                        .publicHolidays(detail.getPublicHolidays())
                        .publicHolidayDates(detail.getPublicHolidayDates()) // ✅ ici
                        .numberOfResources(detail.getNumberOfResources())
                        .totalEstimatedWorkload(detail.getTotalEstimatedWorkload())
                        .totalWorkload(detail.getTotalWorkload())
                        .note(detail.getNote())
                        .devis(devis)
                        .demande(demande)
                        .build()
        ).collect(Collectors.toList()));



        // Invoicing details
        if (dto.getInvoicingDetails() != null) {
            devis.setInvoicingDetails(dto.getInvoicingDetails().stream().map(detail ->
                    new InvoicingDetail(null, detail.getDescription(), detail.getInvoicingDate(),
                            detail.getAmount(), devis, demande)
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
