package com.workpilot.service.PSR.PSR;

import com.workpilot.dto.PsrDTO.DeliveriesDTO;
import com.workpilot.dto.PsrDTO.PsrDTO;
import com.workpilot.dto.PsrDTO.RisksDTO;
import com.workpilot.dto.PsrDTO.TeamOrganizationDTO;
import com.workpilot.entity.PSR.Psr;

import com.workpilot.entity.PSR.TaskTracker;
import com.workpilot.entity.ressources.Project;
import com.workpilot.repository.Psr.TaskTrackerRepository;
import com.workpilot.repository.ressources.ProjectRepository;
import com.workpilot.repository.Psr.PsrRepository;
import com.workpilot.repository.ressources.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
@Service
public class PsrServiceImpl implements PsrService {

    @Autowired
    private PsrRepository psrRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskTrackerRepository taskTrackerRepository;
    @Override
    public PsrDTO createPsr(PsrDTO psrDTO) {
        Psr psr = new Psr();
// Validation de la date
        validateReportDate(psrDTO.getReportDate(), psrDTO.getWeek());
        // 🔹 Calcul automatique de la semaine et de l’année à partir de reportDate
        LocalDate reportDate = psrDTO.getReportDate();
        String week = getWeekFromDate(reportDate);
        int reportYear = reportDate.getYear();

        // 🔒 Vérification unicité
        if (psrRepository.existsByProjectIdAndWeekAndReportYear(psrDTO.getProjectId(), week, reportYear)) {
            throw new IllegalStateException("Un PSR pour cette semaine existe déjà pour ce projet.");
        }

        // ✅ Remplissage des données
        psr.setReportTitle(psrDTO.getReportTitle());
        psr.setReportDate(reportDate);
        psr.setComments(psrDTO.getComments());
        psr.setOverallStatus(psrDTO.getOverallStatus());
        psr.setAuthorName(psrDTO.getAuthorName());

        psr.setPreparedBy(psrDTO.getPreparedBy());
        psr.setValidatedBy(psrDTO.getValidatedBy());
        psr.setApprovedBy(psrDTO.getApprovedBy());

        psr.setReference(psrDTO.getReference());
        psr.setEdition(psrDTO.getEdition());

        psr.setPreparedByDate(psrDTO.getPreparedByDate());
        psr.setApprovedByDate(psrDTO.getApprovedByDate());
        psr.setValidatedByDate(psrDTO.getValidatedByDate());

        // 🔗 Projet
        if (psrDTO.getProjectId() != null) {
            Project project = projectRepository.findById(psrDTO.getProjectId())
                    .orElseThrow(() -> new EntityNotFoundException("Project not found"));
            psr.setProject(project);
            psr.setProjectName(project.getName());
            if (project.getClient() != null) {
                psr.setClientName(project.getClient().getSalesManagers() != null ?
                        String.join(", ", project.getClient().getSalesManagers()) : "");

            }
        }

        // 🔄 Affectation automatique de la semaine et de l’année
        psr.setWeek(week);
        psr.setReportYear(reportYear);

        // ✅ Sauvegarde
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
        Psr psr = psrRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PSR non trouvé avec l'id : " + id));

        // 🔁 Supprimer tous les TaskTrackers liés à ce PSR
        List<TaskTracker> trackers = taskTrackerRepository.findByPsr(psr);
        taskTrackerRepository.deleteAll(trackers);

        // ❌ Ensuite supprimer le PSR
        psrRepository.delete(psr);
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
        dto.setReportYear(psr.getReportYear());

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

    private String getWeekFromDate(LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekNumber = date.get(weekFields.weekOfWeekBasedYear());
        int year = date.getYear();
        return String.format("%d-W%02d", year, weekNumber); // Format: "2024-W01"
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

        // Ne pas permettre la modification de la semaine
        if (!psr.getWeek().equals(psrDTO.getWeek())) {
            throw new IllegalStateException("La semaine ne peut pas être modifiée");
        }
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

    @Override
    public boolean existsPsrForCurrentWeek(Long projectId) {
        LocalDate now = LocalDate.now();
        String currentWeek = getWeekFromDate(now);
        int currentYear = now.getYear();
        return psrRepository.existsByProjectIdAndWeekAndReportYear(projectId, currentWeek, currentYear);
    }

    @Override
    public PsrDTO createCurrentWeekPsr(Long projectId) {
        LocalDate now = LocalDate.now();
        String currentWeek = getWeekFromDate(now);

        // Vérifier si un PSR existe déjà pour cette semaine
        if (existsPsrForCurrentWeek(projectId)) {
            throw new IllegalStateException("Un PSR existe déjà pour la semaine courante");
        }

        // Créer un nouveau PSR pour la semaine courante
        PsrDTO newPsrDTO = new PsrDTO();
        newPsrDTO.setProjectId(projectId);
        newPsrDTO.setReportDate(now);
        newPsrDTO.setReportTitle("PSR - Semaine " + currentWeek);
        newPsrDTO.setOverallStatus("En cours");
        newPsrDTO.setWeek(currentWeek);
        newPsrDTO.setReportYear(now.getYear());

        return createPsr(newPsrDTO);
    }

    @Override
    public List<PsrDTO> getPsrsByWeekRange(Long projectId, String startWeek, String endWeek) {
        return psrRepository.findByProjectIdAndWeekBetween(projectId, startWeek, endWeek)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PsrDTO> getHistoricalPsrs(Long projectId, String week) {
        return psrRepository.findByProjectIdAndWeekLessThanEqual(projectId, week)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private void validateReportDate(LocalDate reportDate, String week) {
        String calculatedWeek = getWeekFromDate(reportDate);
        if (!calculatedWeek.equals(week)) {
            throw new IllegalArgumentException("La date du rapport doit correspondre à la semaine spécifiée");
        }
    }
}
