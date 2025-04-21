package com.workpilot.service.GestionProject.demande;

import com.workpilot.dto.DemandeDTO;
import com.workpilot.entity.devis.Devis;
import com.workpilot.entity.devis.FinancialDetail;
import com.workpilot.entity.devis.InvoicingDetail;
import com.workpilot.entity.ressources.Demande;
import com.workpilot.entity.ressources.Project;
import com.workpilot.entity.ressources.Team;
import com.workpilot.entity.ressources.TeamMember;
import com.workpilot.exception.ResourceNotFoundException;
import com.workpilot.repository.ClientRepository;
import com.workpilot.repository.DemandeRepository;
import com.workpilot.repository.ProjectRepository;
import com.workpilot.repository.TeamMemberRepository;
import com.workpilot.repository.TeamRepository;
import com.workpilot.repository.devis.DevisRepository;
import com.workpilot.repository.devis.FinancialDetailRepository;
import com.workpilot.repository.devis.InvoicingDetailRepository;
import com.workpilot.repository.devis.WorkloadDetailRepository;
import com.workpilot.service.DevisServices.FinancialDetail.FinancialDetailService;
import com.workpilot.service.DevisServices.InvoicingDetail.InvoicingDetailService;
import com.workpilot.service.DevisServices.WorkloadDetail.WorkloadDetailService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.workpilot.entity.ressources.Status;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class DemandeServiceImpl implements DemandeService {

    @Autowired private DemandeRepository demandeRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private TeamMemberRepository teamMemberRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private DevisRepository devisRepository;
    @Autowired private FinancialDetailRepository financialDetailRepository;
    @Autowired private InvoicingDetailRepository invoicingDetailRepository;
    @Autowired private WorkloadDetailRepository workloadDetailRepository;

    @Autowired private FinancialDetailService financialDetailService;
    @Autowired private InvoicingDetailService invoicingDetailService;
    @Autowired private WorkloadDetailService workloadDetailService;


    private static final Set<LocalDate> FIXED_HOLIDAYS = Set.of(
            LocalDate.of(LocalDate.now().getYear(), 1, 1),
            LocalDate.of(LocalDate.now().getYear(), 3, 20),
            LocalDate.of(LocalDate.now().getYear(), 4, 9),
            LocalDate.of(LocalDate.now().getYear(), 5, 1),
            LocalDate.of(LocalDate.now().getYear(), 7, 25),
            LocalDate.of(LocalDate.now().getYear(), 8, 13),
            LocalDate.of(LocalDate.now().getYear(), 10, 15),
            LocalDate.of(LocalDate.now().getYear(), 12, 17)
    );
    @Override
    public List<DemandeDTO> getAlldemandes() {
        List<Demande> demandes = demandeRepository.findAll();
        return demandes.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public DemandeDTO createDemande(DemandeDTO demandeDTO) {
        // 1️⃣ Charger le projet et les membres
        Project project = projectRepository.findById(demandeDTO.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        Set<TeamMember> teamMembers = new HashSet<>();
        if (demandeDTO.getTeamMemberIds() != null && !demandeDTO.getTeamMemberIds().isEmpty()) {
            teamMembers.addAll(teamMemberRepository.findAllById(demandeDTO.getTeamMemberIds()));
        }

        // 2️⃣ Créer la demande
        Demande demande = new Demande();
        demande.setName(demandeDTO.getName());
        demande.setDateDebut(demandeDTO.getDateDebut());
        demande.setDateFin(demandeDTO.getDateFin());
        demande.setScope(demandeDTO.getScope());
        demande.setRequirements(demandeDTO.getRequirements());
        demande.setProject(project);
        demande.setTeamMembers(new ArrayList<>(teamMembers));
        demande = demandeRepository.save(demande);

        // 3️⃣ Créer la team générée automatiquement
        Team generatedTeam = new Team();
        generatedTeam.setName("Team - " + demandeDTO.getName());
        generatedTeam.setMembers(teamMembers);
        // Assurer que l'équipe est liée au projet
        generatedTeam.getProjects().add(project);
        // Ajouter la team dans le côté bidirectionnel des TeamMember
        for (TeamMember member : teamMembers) {
            member.getTeams().add(generatedTeam);
        }
        generatedTeam = teamRepository.save(generatedTeam);

        // 4️⃣ Lier la team générée à la demande
        demande.setGeneratedTeam(generatedTeam);
        demande = demandeRepository.save(demande);

        // 5️⃣ Lier la team au projet (si ce n'est pas déjà le cas)
        project.getTeams().add(generatedTeam);
        projectRepository.save(project);

        // 6️⃣ Générer automatiquement un devis pour la demande
        Devis generatedDevis = createGeneratedDevisForDemande(demande);
        // Lier le devis à la demande (en supposant que Demande possède un attribut generatedDevis)
        demande.setGeneratedDevis(generatedDevis);
        demande = demandeRepository.save(demande);

        // 7️⃣ Mettre à jour les dates du projet
        updateProjectDates(project.getId());

        // 8️⃣ Retourner le DTO avec les IDs générés (team et devis)
        DemandeDTO dto = convertToDTO(demande);
        dto.setGeneratedTeamId(generatedTeam.getId());
        dto.setGeneratedDevisId(generatedDevis.getId());

        return dto;
    }

    /**
     * Méthode pour créer un devis généré automatiquement.
     */

    private Devis createGeneratedDevisForDemande(Demande demande) {
        Project project = demande.getProject();

        Devis devis = new Devis();
        devis.setReference("Devis" + System.currentTimeMillis());
        devis.setCreationDate(LocalDate.now());
        devis.setProject(project);
        devis.setAuthor(project.getUser().getFirstname());

        Devis savedDevis = devisRepository.save(devis);

        // ✅ Membres uniquement de la demande
        List<TeamMember> allMembers = new ArrayList<>(demande.getTeamMembers());

        // Définir la période de départ
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        Month month = now.getMonth();



        // 🔁 Génération financière avec uniquement les membres de la demande
        List<FinancialDetail> details = financialDetailService.generateFromTeamMembers(
                allMembers,
                year,
                month,
                FIXED_HOLIDAYS
        );

        for (FinancialDetail detail : details) {
            detail.setDevis(savedDevis);
        }
        financialDetailRepository.saveAll(details);

        // Reste inchangé : Invoicing + Workload
        BigDecimal totalCost = details.stream()
                .map(FinancialDetail::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthlyAmount = totalCost.divide(BigDecimal.valueOf(3), RoundingMode.HALF_UP);
        LocalDate baseDate = LocalDate.now();

        List<InvoicingDetail> invoicingDetails = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            InvoicingDetail invoice = new InvoicingDetail();
            invoice.setDescription(" " + baseDate.plusMonths(i).getMonth());
            invoice.setInvoicingDate(baseDate.plusMonths(i));
            invoice.setAmount(monthlyAmount);
            invoice.setDevis(savedDevis);
            invoicingDetails.add(invoice);
        }

        invoicingDetailRepository.saveAll(invoicingDetails);

        workloadDetailService.generateFromDemandes(savedDevis.getId());

        return savedDevis;
    }

    @Override
    public DemandeDTO updateDemande(Long id, DemandeDTO demandeDTO) {
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande not found"));

        // 🔄 Mise à jour des données
        demande.setName(demandeDTO.getName());
        demande.setDateDebut(demandeDTO.getDateDebut());
        demande.setDateFin(demandeDTO.getDateFin());

        Project project = projectRepository.findById(demandeDTO.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        demande.setProject(project);

        if (demandeDTO.getTeamMemberIds() != null) {
            Set<TeamMember> teamMembers = new HashSet<>(teamMemberRepository.findAllById(demandeDTO.getTeamMemberIds()));
            demande.setTeamMembers(new ArrayList<>(teamMembers));
        }

        demande = demandeRepository.save(demande);

        // 🔁 Mise à jour du devis associé
        if (demande.getGeneratedDevis() != null) {
            Devis generatedDevis = demande.getGeneratedDevis();
            Long devisId = generatedDevis.getId();

            // Supprimer les anciens
            financialDetailRepository.deleteByDevis_Id(devisId);

            invoicingDetailRepository.deleteByDevis_Id(devisId);

            workloadDetailRepository.deleteAllByDevisId(devisId);

            // Re-générer FinancialDetail
            List<FinancialDetail> financials = financialDetailService.generateFromTeamMembers(
                    new ArrayList<>(demande.getTeamMembers()),
                    LocalDate.now().getYear(),
                    LocalDate.now().getMonth(),
                    FIXED_HOLIDAYS
            );
            financials.forEach(f -> f.setDevis(generatedDevis));
            financialDetailRepository.saveAll(financials);

            // Re-générer InvoicingDetail
            List<InvoicingDetail> invoices = invoicingDetailService.generateInvoicingDetails(devisId, LocalDate.now().getMonthValue())
                    .stream()
                    .map(dto -> {
                        InvoicingDetail i = new InvoicingDetail();
                        i.setDescription(dto.getDescription());
                        i.setInvoicingDate(dto.getInvoicingDate());
                        i.setAmount(dto.getAmount());
                        i.setDevis(generatedDevis);
                        return i;
                    }).toList();

            invoicingDetailRepository.saveAll(invoices);


            // Re-générer Workload
            workloadDetailService.generateFromDemandes(devisId);
        }


        updateProjectDates(project.getId());
        return convertToDTO(demande);
    }

    @Override
    @Transactional
    public void deleteDemande(Long id) {
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande not found"));

        // Détacher les membres
        if (demande.getTeamMembers() != null && !demande.getTeamMembers().isEmpty()) {
            demande.getTeamMembers().clear();
            demandeRepository.save(demande);
        }

        // Supprimer la team générée
        if (demande.getGeneratedTeam() != null) {
            Team generatedTeam = demande.getGeneratedTeam();

            // Détacher membres
            generatedTeam.getMembers().clear();

            // ✅ Détacher la team de tous les projets (clé étrangère)
            for (Project project : new HashSet<>(generatedTeam.getProjects())) {
                project.getTeams().remove(generatedTeam);
            }
            generatedTeam.getProjects().clear();

            demande.setGeneratedTeam(null);
            demandeRepository.save(demande);
            teamRepository.save(generatedTeam);
            teamRepository.delete(generatedTeam);
        }

        // Supprimer le devis généré
        if (demande.getGeneratedDevis() != null) {
            devisRepository.delete(demande.getGeneratedDevis());
        }

        // Supprimer la demande
        demandeRepository.delete(demande);

        // Mise à jour des dates du projet
        updateProjectDates(demande.getProject().getId());
    }




    @Override
    public List<DemandeDTO> getDemandesByProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project with ID " + projectId + " not found");
        }

        List<Demande> demandes = demandeRepository.findByProjectId(projectId);

        // Affichage en console pour debugging
        System.out.println("📦 Récupération des demandes du projet ID = " + projectId);
        if (demandes.isEmpty()) {
            System.out.println("➡️ Aucune demande trouvée.");
        } else {
            demandes.forEach(d -> {
                String members = d.getTeamMembers().stream()
                        .map(m -> m.getName() + " (ID=" + m.getId() + ")")
                        .collect(Collectors.joining(", "));
                System.out.println("➡️ Demande ID = " + d.getId() + ", Nom = " + d.getName() + ", Membres = " + members);
            });
        }

        return demandes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private DemandeDTO convertToDTO(Demande demande) {
        return new DemandeDTO(
                demande.getId(),
                demande.getName(),
                demande.getDateDebut(),
                demande.getDateFin(),
                demande.getProject() != null ? demande.getProject().getId() : null,
                demande.getProject() != null ? demande.getProject().getName() : "Projet inconnu",
                demande.getTeamMembers() != null
                        ? demande.getTeamMembers().stream().map(TeamMember::getId).collect(Collectors.toSet())
                        : new HashSet<>(),
                demande.getScope(),
                demande.getRequirements(),
                demande.getGeneratedTeam() != null ? demande.getGeneratedTeam().getId() : null,
                demande.getGeneratedDevis() != null ? demande.getGeneratedDevis().getId() : null
        );
    }



    private void updateProjectDates(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        List<Demande> demandes = demandeRepository.findByProjectId(projectId);
        System.out.println("Nombre de demandes trouvées pour le projet " + projectId + ": " + demandes.size());

        if (!demandes.isEmpty()) {
            LocalDate earliestStart = demandes.stream()
                    .map(Demande::getDateDebut)
                    .min(LocalDate::compareTo)
                    .orElse(project.getStartDate());

            LocalDate latestEnd = demandes.stream()
                    .map(Demande::getDateFin)
                    .max(LocalDate::compareTo)
                    .orElse(project.getEndDate());

            System.out.println("Date de début calculée : " + earliestStart);
            System.out.println("Date de fin calculée : " + latestEnd);

            project.setStartDate(earliestStart);
            project.setEndDate(latestEnd);

            // ✅ Mise à jour automatique du statut (selon enum Status)
            LocalDate today = LocalDate.now();
            if (latestEnd.isBefore(today)) {
                project.setStatus(Status.TERMINE);
            } else if (earliestStart.isAfter(today)) {
                project.setStatus(Status.EN_ATTENTE);
            } else {
                project.setStatus(Status.EN_COURS);
            }

            projectRepository.save(project);
        }
    }


}
