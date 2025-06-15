package com.workpilot.service.PSR.PSR;

import com.workpilot.dto.PsrDTO.*;
import com.workpilot.entity.PSR.Psr;
import com.workpilot.entity.PSR.PsrGenerationLog;
import com.workpilot.entity.PSR.TaskTracker;
import com.workpilot.entity.ressources.Project;
import com.workpilot.repository.Psr.PsrGenerationLogRepository;
import com.workpilot.repository.Psr.TaskTrackerRepository;
import com.workpilot.repository.Psr.WeeklyReportRepository;
import com.workpilot.repository.ressources.ProjectRepository;
import com.workpilot.repository.Psr.PsrRepository;
import com.workpilot.repository.ressources.UserRepository;
import com.workpilot.service.NotificationService;
import com.workpilot.service.PSR.TaskTracker.TaskTrackerService;
import com.workpilot.service.PSR.TeamOrganization.TeamOrganizationService;
import com.workpilot.service.PSR.weeklyReport.WeeklyReportService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class PsrServiceImpl implements PsrService {

    private static final Logger logger = LoggerFactory.getLogger(PsrServiceImpl.class);

    @Autowired
    private PsrRepository psrRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskTrackerRepository taskTrackerRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private PsrGenerationLogRepository logRepository;

    @Autowired
    private WeeklyReportRepository weeklyReportRepository;

    @Autowired
    private TeamOrganizationService teamOrganizationService;

    @Autowired
    private WeeklyReportService weeklyReportService;

    @Autowired
    private TaskTrackerService taskTrackerService;
    @Override
    public PsrDTO createPsr(PsrDTO psrDTO) {
        logger.info("Création d'un PSR pour le projet ID: {}", psrDTO.getProjectId());
        Psr psr = new Psr();
        String calculatedWeek = getWeekFromDate(psrDTO.getReportDate());
        psr.setWeek(calculatedWeek);

        LocalDate reportDate = psrDTO.getReportDate();
        String week = getWeekFromDate(reportDate);
        int reportYear = reportDate.getYear();

        if (psrRepository.existsByProjectIdAndWeekAndReportYear(psrDTO.getProjectId(), week, reportYear)) {
            logger.warn("Un PSR existe déjà pour le projet ID: {} et la semaine: {}", psrDTO.getProjectId(), week);
            throw new IllegalStateException("Un PSR pour cette semaine existe déjà pour ce projet.");
        }

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

        psr.setWeek(week);
        psr.setReportYear(reportYear);

        Psr saved = psrRepository.save(psr);
        logger.debug("PSR sauvegardé avec ID: {}", saved.getId());

        // Associer les membres pour la semaine du PSR
        try {
            List<TeamOrganizationDTO> members = teamOrganizationService.getAllProjectMembersForPsr(saved.getId());
            logger.info("Associé {} membres au PSR ID: {}", members.size(), saved.getId());
        } catch (Exception e) {
            logger.error("Erreur lors de l'association des membres au PSR ID: {}: {}", saved.getId(), e.getMessage());
        }

        // Envoyer une notification via NotificationService
        try {
            String title = "Nouveau PSR généré";
            String message = "📄 PSR généré pour le projet : " + psr.getProjectName() + " - Semaine " + week;
            notificationService.sendNotification(title, message, "ADMIN"); // Rôle ciblé, ajustez selon besoin
            logger.info("Notification envoyée pour PSR ID: {}", saved.getId());
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de la notification pour PSR ID: {}: {}", saved.getId(), e.getMessage());
        }

        return convertToDTO(saved);
    }

    @Override
    public List<PsrDTO> getAllPsrs() {
        logger.info("Récupération de tous les PSR");
        return psrRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PsrDTO getPsrById(Long id) {
        logger.info("Récupération du PSR ID: {}", id);
        Psr psr = psrRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PSR not found"));
        return convertToDTO(psr);
    }

    @Override
    public void deletePsr(Long id) {
        logger.info("Suppression du PSR ID: {}", id);
        Psr psr = psrRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PSR non trouvé avec l'id : " + id));
        weeklyReportRepository.deleteByPsrId(id);
        List<TaskTracker> trackers = taskTrackerRepository.findByPsr(psr);
        taskTrackerRepository.deleteAll(trackers);
        psrRepository.delete(psr);
    }

    private PsrDTO convertToDTO(Psr psr) {
        PsrDTO dto = new PsrDTO();

        // Champs simples
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

        // Risks
        if (psr.getRisks() != null && !psr.getRisks().isEmpty()) {
            List<RisksDTO> risks = psr.getRisks().stream().map(risk -> {
                RisksDTO riskDTO = new RisksDTO();
                riskDTO.setId(risk.getId());
                riskDTO.setDescription(risk.getDescription());
                riskDTO.setOrigin(risk.getOrigin());
                riskDTO.setCategory(risk.getCategory());
                riskDTO.setOpenDate(String.valueOf(risk.getOpenDate()));
                riskDTO.setDueDate(String.valueOf(risk.getDueDate()));
                riskDTO.setCauses(risk.getCauses());
                riskDTO.setConsequences(risk.getConsequences());
                riskDTO.setAppliedMeasures(risk.getAppliedMeasures());

                riskDTO.setProbability(risk.getProbability());
                riskDTO.setGravity(risk.getGravity());
                riskDTO.setCriticality(risk.getCriticality());
                riskDTO.setMeasure(risk.getMeasure());

                riskDTO.setRiskTreatmentDecision(risk.getRiskTreatmentDecision());
                riskDTO.setJustification(risk.getJustification());
                riskDTO.setIdAction(risk.getIdAction());
                riskDTO.setRiskStat(risk.getRiskStat());
                riskDTO.setCloseDate(String.valueOf(risk.getCloseDate()));

                // Optionnel : champs supplémentaires
                riskDTO.setImpact(risk.getImpact());
                riskDTO.setMitigationPlan(risk.getMitigationPlan());
                riskDTO.setWeek(risk.getWeek());
                riskDTO.setReportYear(risk.getReportYear());

                return riskDTO;
            }).collect(Collectors.toList());
            dto.setRisks(risks);
        }


        // Deliveries
        if (psr.getDeliveries() != null && !psr.getDeliveries().isEmpty()) {
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

        // Team Organizations
        if (psr.getTeamOrganizations() != null && !psr.getTeamOrganizations().isEmpty()) {
            List<TeamOrganizationDTO> teamOrganizations = psr.getTeamOrganizations().stream().map(team -> {
                TeamOrganizationDTO teamDTO = new TeamOrganizationDTO();
                teamDTO.setId(team.getId());
                teamDTO.setFullName(team.getFullName());
                teamDTO.setInitial(team.getInitial());
                teamDTO.setRole(team.getRole());
                teamDTO.setProject(team.getProject());
                teamDTO.setPlannedStartDate(team.getPlannedStartDate());
                teamDTO.setPlannedEndDate(team.getPlannedEndDate());
                teamDTO.setAllocation(team.getAllocation());
                teamDTO.setComingFromTeam(team.getComingFromTeam());
                teamDTO.setGoingToTeam(team.getGoingToTeam());
                teamDTO.setHoliday(team.getHoliday());
                teamDTO.setTeamName(team.getTeamName());
                teamDTO.setWeek(team.getWeek());
                return teamDTO;
            }).collect(Collectors.toList());
            dto.setTeamOrganizations(teamOrganizations);
        }

        // Task Trackers
        if (psr.getTaskTrackers() != null && !psr.getTaskTrackers().isEmpty()) {
            List<TaskTrackerDTO> taskTrackers = psr.getTaskTrackers().stream().map(task -> {
                TaskTrackerDTO taskDTO = new TaskTrackerDTO();
                taskDTO.setId(task.getId());
                taskDTO.setDescription(task.getDescription());
                taskDTO.setPsrId(psr.getId());
                taskDTO.setProjectId(psr.getProject() != null ? psr.getProject().getId() : null);
                taskDTO.setWeek(task.getWeek());
                taskDTO.setWho(task.getWho());
                taskDTO.setStartDate(task.getStartDate());
                taskDTO.setEstimatedEndDate(task.getEstimatedEndDate());
                taskDTO.setEffectiveEndDate(task.getEffectiveEndDate());
                taskDTO.setWorkedMD(task.getWorkedMD());
                taskDTO.setEstimatedMD(task.getEstimatedMD());
                taskDTO.setRemainingMD(task.getRemainingMD());
                taskDTO.setProgress(task.getProgress());
                taskDTO.setCurrentStatus(task.getCurrentStatus());
                taskDTO.setEffortVariance(task.getEffortVariance());
                taskDTO.setDeviationReason(task.getDeviationReason());
                taskDTO.setNote(task.getNote());
                return taskDTO;
            }).collect(Collectors.toList());
            dto.setTaskTrackers(taskTrackers);
        }

        // Weekly Reports
        if (psr.getWeeklyReports() != null && !psr.getWeeklyReports().isEmpty()) {
            List<WeeklyReportDTO> weeklyReports = psr.getWeeklyReports().stream().map(report -> {
                WeeklyReportDTO reportDTO = new WeeklyReportDTO();
                reportDTO.setId(report.getId());
                reportDTO.setMonth(report.getMonth());
                reportDTO.setWeekNumber(report.getWeekNumber());
                reportDTO.setYear(report.getYear());
                reportDTO.setProjectName(report.getProjectName());
                reportDTO.setWorkingDays(report.getWorkingDays());
                reportDTO.setEstimatedDays(report.getEstimatedDays());
                reportDTO.setEffortVariance(report.getEffortVariance());
                reportDTO.setPsrId(psr.getId());
                return reportDTO;
            }).collect(Collectors.toList());
            dto.setWeeklyReports(weeklyReports);
        }

        return dto;
    }


    private String getWeekFromDate(LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekNumber = date.get(weekFields.weekOfWeekBasedYear());
        int year = date.getYear();
        return String.format("%d-W%02d", year, weekNumber);
    }

    @Override
    public List<PsrDTO> getPsrsByProject(Long projectId) {
        logger.info("Récupération des PSR pour le projet ID: {}", projectId);
        List<Psr> psrs = psrRepository.findByProjectId(projectId);
        return psrs.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public PsrDTO updatePsr(Long id, PsrDTO psrDTO) {
        logger.info("Mise à jour du PSR ID: {}", id);
        Psr psr = psrRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PSR non trouvé avec l'id : " + id));
        if (!psr.getWeek().equals(psrDTO.getWeek())) {
            throw new IllegalStateException("La semaine ne peut pas être modifiée");
        }
        psr.setReportTitle(psrDTO.getReportTitle());
        psr.setReportDate(psrDTO.getReportDate());
        psr.setComments(psrDTO.getComments());
        psr.setOverallStatus(psrDTO.getOverallStatus());
        psr.setAuthorName(psrDTO.getAuthorName());
        psr.setPreparedBy(psrDTO.getPreparedBy());
        psr.setApprovedBy(psrDTO.getApprovedBy());
        psr.setValidatedBy(psrDTO.getValidatedBy());
        psr.setPreparedByDate(psrDTO.getPreparedByDate());
        psr.setApprovedByDate(psrDTO.getApprovedByDate());
        psr.setValidatedByDate(psrDTO.getValidatedByDate());
        psr.setReference(psrDTO.getReference());
        psr.setEdition(psrDTO.getEdition());
        psr.setWeek(psrDTO.getWeek());

        if (psrDTO.getProjectId() != null) {
            Project project = projectRepository.findById(psrDTO.getProjectId())
                    .orElseThrow(() -> new EntityNotFoundException("Project non trouvé avec l'id : " + psrDTO.getProjectId()));
            psr.setProject(project);
            psr.setProjectName(project.getName());
            if (project.getClient() != null) {
                psr.setClientName(String.join(", ", project.getClient().getSalesManagers()));
            }
        }

        Psr updated = psrRepository.save(psr);
        logger.debug("PSR mis à jour avec ID: {}", updated.getId());
        return convertToDTO(updated);
    }

    @Override
    public boolean existsPsrForCurrentWeek(Long projectId) {
        LocalDate now = LocalDate.now();
        String currentWeek = getWeekFromDate(now);
        int currentYear = now.getYear();
        logger.debug("Vérification de l'existence d'un PSR pour le projet ID: {} et la semaine: {}", projectId, currentWeek);
        return psrRepository.existsByProjectIdAndWeekAndReportYear(projectId, currentWeek, currentYear);
    }

    @Override
    public PsrDTO createCurrentWeekPsr(Long projectId) {
        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.minusWeeks(1);
        String targetWeek = getWeekFromDate(targetDate);
        int targetYear = targetDate.getYear();

        logger.info("Création d'un PSR pour le projet ID: {} et la semaine: {}", projectId, targetWeek);
        if (psrRepository.existsByProjectIdAndWeekAndReportYear(projectId, targetWeek, targetYear)) {
            logger.warn("Un PSR existe déjà pour le projet ID: {} et la semaine: {}", projectId, targetWeek);
            throw new IllegalStateException("Un PSR existe déjà pour la semaine : " + targetWeek);
        }

        PsrDTO newPsrDTO = new PsrDTO();
        newPsrDTO.setProjectId(projectId);
        newPsrDTO.setReportDate(targetDate);
        newPsrDTO.setReportTitle("PSR - Semaine " + targetWeek);
        newPsrDTO.setOverallStatus("En cours");
        newPsrDTO.setWeek(targetWeek);
        newPsrDTO.setReportYear(targetYear);

        return createPsr(newPsrDTO);
    }

    @Override
    public List<PsrDTO> getPsrsByWeekRange(Long projectId, String startWeek, String endWeek) {
        logger.info("Récupération des PSR pour le projet ID: {} entre les semaines {} et {}", projectId, startWeek, endWeek);
        return psrRepository.findByProjectIdAndWeekBetween(projectId, startWeek, endWeek)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PsrDTO> getHistoricalPsrs(Long projectId, String week) {
        logger.info("Récupération des PSR historiques pour le projet ID: {} jusqu'à la semaine: {}", projectId, week);
        return psrRepository.findByProjectIdAndWeekLessThanEqual(projectId, week)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private void validateReportDate(LocalDate reportDate, String week) {
        String calculatedWeek = getWeekFromDate(reportDate);
        if (!calculatedWeek.equals(week)) {
            logger.warn("La date du rapport {} ne correspond pas à la semaine spécifiée: {}", reportDate, week);
            throw new IllegalArgumentException("La date du rapport doit correspondre à la semaine spécifiée");
        }
    }

    @Override
    public List<PsrDTO> getCurrentWeekPsrs(Long projectId) {
        LocalDate today = LocalDate.now();
        String currentWeek = getWeekFromDate(today);
        int year = today.getYear();
        logger.info("Récupération des PSR pour le projet ID: {} et la semaine: {}", projectId, currentWeek);
        return psrRepository.findByProjectIdAndWeekAndReportYear(projectId, currentWeek, year)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Scheduled(cron = "0 0 10 * * MON")
    public void autoGenerateWeeklyPsrsForAllProjects() {
        logger.info("Démarrage de la génération automatique des PSR à {}", LocalDateTime.now());
        List<Project> allProjects = projectRepository.findAll();
        logger.debug("Nombre de projets trouvés: {}", allProjects.size());

        LocalDate today = LocalDate.now();
        LocalDate previousWeekDate = today.minusWeeks(1);
        String weekToGenerate = getWeekFromDate(previousWeekDate);
        int year = previousWeekDate.getYear();

        for (Project project : allProjects) {
            Long projectId = project.getId();
            logger.debug("Traitement du projet: {} (ID: {})", project.getName(), projectId);

            if (project.getStartDate() != null && today.isBefore(project.getStartDate())) {
                logger.debug("Projet non commencé: {}", project.getName());
                continue;
            }
            if (project.getEndDate() != null && today.isAfter(project.getEndDate())) {
                logger.debug("Projet terminé: {}", project.getName());
                continue;
            }

            PsrGenerationLog log = new PsrGenerationLog();
            log.setProjectId(projectId);
            log.setProjectName(project.getName());
            log.setWeek(weekToGenerate);
            log.setYear(year);
            log.setGeneratedAt(LocalDateTime.now());

            boolean alreadyExists = psrRepository.existsByProjectIdAndWeekAndReportYear(projectId, weekToGenerate, year);
            if (!alreadyExists) {
                try {
                    PsrDTO newPsrDTO = new PsrDTO();
                    newPsrDTO.setProjectId(projectId);
                    newPsrDTO.setReportDate(previousWeekDate);
                    newPsrDTO.setReportTitle("PSR - Semaine " + weekToGenerate);
                    newPsrDTO.setOverallStatus("En cours");
                    newPsrDTO.setWeek(weekToGenerate);
                    newPsrDTO.setReportYear(year);

                    // Création du PSR (ne génère plus le WeeklyReport ici)
                    PsrDTO createdPsr = createPsr(newPsrDTO);

                    // Charger l'entité Psr
                    Psr psrEntity = psrRepository.findById(createdPsr.getId())
                            .orElseThrow(() -> new EntityNotFoundException("PSR not found"));

                    // Synchroniser les tâches
                    taskTrackerService.synchronizeTaskTrackers(psrEntity);

                    // Générer le Weekly Report APRÈS la synchro
                    weeklyReportService.generateReportFromPsr(createdPsr.getId());

                    log.setStatus("SUCCESS");
                    log.setMessage("PSR généré pour la semaine précédente, ID: " + createdPsr.getId());
                } catch (Exception e) {
                    log.setStatus("ERROR");
                    log.setMessage("Erreur génération PSR : " + e.getMessage());
                    logger.error("Erreur lors de la génération du PSR pour le projet ID: {}: {}", projectId, e.getMessage());
                }
            } else {
                log.setStatus("ALREADY_EXISTS");
                log.setMessage("PSR déjà existant pour cette semaine.");
                logger.debug("PSR déjà existant pour le projet ID: {} et la semaine: {}", projectId, weekToGenerate);
            }

            try {
                logRepository.save(log);
                logger.debug("Log sauvegardé pour le projet ID: {}", projectId);
            } catch (Exception e) {
                logger.error("Erreur lors de la sauvegarde du log pour le projet ID: {}: {}", projectId, e.getMessage());
            }
        }
        logger.info("Fin de la génération automatique des PSR");
    }
}
