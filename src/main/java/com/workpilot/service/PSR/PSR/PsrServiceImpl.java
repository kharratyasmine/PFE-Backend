package com.workpilot.service.PSR.PSR;

import com.workpilot.dto.PsrDTO.DeliveriesDTO;
import com.workpilot.dto.PsrDTO.PsrDTO;
import com.workpilot.dto.PsrDTO.RisksDTO;
import com.workpilot.dto.PsrDTO.TeamOrganizationDTO;
import com.workpilot.entity.PSR.Psr;

import com.workpilot.entity.ressources.Project;
import com.workpilot.repository.ressources.ProjectRepository;
import com.workpilot.repository.Psr.PsrRepository;
import com.workpilot.repository.ressources.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class PsrServiceImpl implements PsrService {

    @Autowired
    private PsrRepository psrRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public PsrDTO createPsr(PsrDTO psrDTO) {
        Psr psr = new Psr();

        // Remplissage des informations de base du PSR depuis le DTO
        psr.setReportTitle(psrDTO.getReportTitle());
        psr.setReportDate(psrDTO.getReportDate());
        psr.setComments(psrDTO.getComments());
        psr.setOverallStatus(psrDTO.getOverallStatus());
        psr.setAuthorName(psrDTO.getAuthorName());

        psr.setPreparedBy(psrDTO.getPreparedBy());
        psr.setValidatedBy(psrDTO.getValidatedBy());
        psr.setApprovedBy(psr.getApprovedBy());

        // Remplissage des informations supplémentaires
        psr.setReference(psrDTO.getReference());  // Référence du PSR
        psr.setEdition(psrDTO.getEdition());  // Edition du PSR

        // Dates pour la préparation, l'approbation et la validation
        psr.setPreparedByDate(psrDTO.getPreparedByDate());
        psr.setApprovedByDate(psrDTO.getApprovedByDate());
        psr.setValidatedByDate(psrDTO.getValidatedByDate());

        // Récupération du projet directement (sans lien avec Devis ou Demande)
        if (psrDTO.getProjectId() != null) {
            Project project = projectRepository.findById(psrDTO.getProjectId())
                    .orElseThrow(() -> new EntityNotFoundException("Project not found"));
            psr.setProject(project);

            // Peupler les informations du projet dans le PSR (si nécessaire)
            psr.setProjectName(project.getName());  // Nom du projet
            if (project.getClient() != null) {
                // Joindre tous les gestionnaires des ventes en une seule chaîne
                psr.setClientName(String.join(", ", project.getClient().getSalesManagers()));
            }

        }

        // Remplissage du champ "week" (si nécessaire)
        if (psrDTO.getWeek() != null) {
            psr.setWeek(psrDTO.getWeek());
        }

        // Sauvegarde du PSR
        Psr saved = psrRepository.save(psr);

        return convertToDTO(saved);
    }

    @Override
    public List<PsrDTO> getAllPsrs() {
        return psrRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PsrDTO getPsrById(Long id) {
        Psr psr = psrRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PSR not found"));
        return convertToDTO(psr);
    }

    @Override
    public void deletePsr(Long id) {
        psrRepository.deleteById(id);
    }

    private PsrDTO convertToDTO(Psr psr) {
        PsrDTO dto = new PsrDTO();
        dto.setId(psr.getId());
        dto.setReportTitle(psr.getReportTitle());
        dto.setReportDate(psr.getReportDate());
        dto.setComments(psr.getComments());
        dto.setOverallStatus(psr.getOverallStatus());
        dto.setReference(psr.getReference());
        dto.setEdition(psr.getEdition());
        dto.setPreparedBy(psr.getPreparedBy());
        dto.setApprovedBy(psr.getApprovedBy());
        dto.setValidatedBy(psr.getValidatedBy());
        dto.setPreparedByDate(psr.getPreparedByDate());
        dto.setApprovedByDate(psr.getApprovedByDate());
        dto.setValidatedByDate(psr.getValidatedByDate());
        dto.setProjectName(psr.getProjectName());
        dto.setClientName(psr.getClientName());
        dto.setWeek(psr.getWeek());
        dto.setAuthorName(psr.getAuthorName());

        if (psr.getProject() != null) {
            dto.setProjectId(psr.getProject().getId());
        }
        if (psr.getRisks() != null) {
            List<RisksDTO> risks = psr.getRisks().stream().map(risk -> {
                RisksDTO riskDTO = new RisksDTO();
                riskDTO.setId(risk.getId());
                riskDTO.setDescription(risk.getDescription());
                riskDTO.setProbability(risk.getProbability());
                riskDTO.setImpact(risk.getImpact());
                riskDTO.setMitigationPlan(risk.getMitigationPlan());
                return riskDTO;
            }).collect(Collectors.toList());
            dto.setRisks(risks);
        }

        if (psr.getDeliveries() != null) {
            List<DeliveriesDTO> deliveries = psr.getDeliveries().stream().map(delivery -> {
                DeliveriesDTO deliveryDTO = new DeliveriesDTO();
                deliveryDTO.setId(delivery.getId());
                deliveryDTO.setDeliveriesName(delivery.getDeliveriesName());
                deliveryDTO.setDescription(delivery.getDescription());
                deliveryDTO.setVersion(delivery.getVersion());
                deliveryDTO.setPlannedDate(delivery.getPlannedDate());
                deliveryDTO.setEffectiveDate(delivery.getEffectiveDate());
                deliveryDTO.setStatus(delivery.getStatus());
                deliveryDTO.setDeliverySupport(delivery.getDeliverySupport());
                deliveryDTO.setCustomerFeedback(delivery.getCustomerFeedback());
                return deliveryDTO;
            }).collect(Collectors.toList());
            dto.setDeliveries(deliveries);
        }

        if (psr.getTeamOrganizations() != null) {
            List<TeamOrganizationDTO> teamOrganization = psr.getTeamOrganizations().stream().map(teamOrganizations -> {
                TeamOrganizationDTO teamOrganizationDTO = new TeamOrganizationDTO();
                teamOrganizationDTO.setId(teamOrganizations.getId());
                teamOrganizationDTO.setFullName(teamOrganizations.getFullName());
                teamOrganizationDTO.setInitial(teamOrganizations.getInitial());
                teamOrganizationDTO.setRole(teamOrganizations.getRole());
                teamOrganizationDTO.setProject(teamOrganizations.getProject());
                teamOrganizationDTO.setPlannedStartDate(teamOrganizations.getPlannedStartDate());
                teamOrganizationDTO.setPlannedEndDate(teamOrganizations.getPlannedEndDate());
                teamOrganizationDTO.setAllocation(teamOrganizations.getAllocation());
                teamOrganizationDTO.setComingFromTeam(teamOrganizations.getComingFromTeam());
                teamOrganizationDTO.setGoingToTeam(teamOrganizations.getGoingToTeam());
                teamOrganizationDTO.setHoliday(teamOrganizations.getHoliday());
                teamOrganizationDTO.setTeamName(teamOrganizations.getTeamName());
                return teamOrganizationDTO;
            }).collect(Collectors.toList());
            dto.setTeamOrganizations(teamOrganization);
        }

        return dto;
    }

    @Override
    public List<PsrDTO> getPsrsByProject(Long projectId) {
        List<Psr> psrs = psrRepository.findByProjectId(projectId);
        return psrs.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public PsrDTO updatePsr(Long id, PsrDTO psrDTO) {
        // Récupérer le PSR existant
        Psr psr = psrRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PSR non trouvé avec l'id : " + id));

        // Mise à jour des champs principaux
        psr.setReportTitle(psrDTO.getReportTitle());
        psr.setReportDate(psrDTO.getReportDate());
        psr.setComments(psrDTO.getComments());
        psr.setOverallStatus(psrDTO.getOverallStatus());
        psr.setAuthorName(psrDTO.getAuthorName());

        // Mise à jour des champs de la cover
        psr.setPreparedBy(psrDTO.getPreparedBy());
        psr.setApprovedBy(psrDTO.getApprovedBy());
        psr.setValidatedBy(psrDTO.getValidatedBy());

        psr.setPreparedByDate(psrDTO.getPreparedByDate());
        psr.setApprovedByDate(psrDTO.getApprovedByDate());
        psr.setValidatedByDate(psrDTO.getValidatedByDate());

        // Mise à jour des informations supplémentaires
        psr.setReference(psrDTO.getReference());
        psr.setEdition(psrDTO.getEdition());
        psr.setWeek(psrDTO.getWeek());

        // Récupérer et mettre à jour le projet, si nécessaire
        if (psrDTO.getProjectId() != null) {
            // Récupérer le projet associé à ce PSR
            Project project = projectRepository.findById(psrDTO.getProjectId())
                    .orElseThrow(() -> new EntityNotFoundException("Project non trouvé avec l'id : " + psrDTO.getProjectId()));

            // Mettre à jour les informations du projet dans le PSR
            psr.setProject(project);
            psr.setProjectName(project.getName());

            // Récupérer le client et son nom
            if (project.getClient() != null) {
                psr.setClientName(String.join(", ", project.getClient().getSalesManagers()));  // Joindre les gestionnaires des ventes
            }
        }

        // Sauvegarder le PSR mis à jour
        Psr updated = psrRepository.save(psr);

        // Retourner le DTO mis à jour
        return convertToDTO(updated);
    }
}
