package com.workpilot.service.GestionRessources.demande;

import com.workpilot.dto.GestionRessources.DemandeDTO;
import com.workpilot.dto.DevisDTO.InvoicingDetailDTO;
import com.workpilot.dto.GestionRessources.FakeMemberDTO;
import com.workpilot.dto.PsrDTO.PsrDTO;
import com.workpilot.entity.devis.Devis;
import com.workpilot.entity.devis.FinancialDetail;
import com.workpilot.entity.devis.InvoicingDetail;
import com.workpilot.entity.ressources.*;
import com.workpilot.exception.ResourceNotFoundException;
import com.workpilot.repository.Psr.PsrRepository;
import com.workpilot.repository.devis.DevisRepository;
import com.workpilot.repository.devis.FinancialDetailRepository;
import com.workpilot.repository.devis.InvoicingDetailRepository;
import com.workpilot.repository.devis.WorkloadDetailRepository;
import com.workpilot.repository.ressources.*;
import com.workpilot.service.DevisServices.FinancialDetail.FinancialDetailService;
import com.workpilot.service.DevisServices.InvoicingDetail.InvoicingDetailService;
import com.workpilot.service.DevisServices.WorkloadDetail.WorkloadDetailService;
import com.workpilot.service.GestionRessources.PlannedWorkloadMember.PlannedWorkloadMemberService;
import com.workpilot.service.GestionRessources.PlannedWorkloadMember.PlannedWorkloadMemberServiceImpl;
import com.workpilot.service.PSR.PSR.PsrService;
import com.workpilot.service.PublicHolidayService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
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
    @Autowired private TeamMemberAllocationRepository teamMemberAllocationRepository;
    @Autowired private FinancialDetailService financialDetailService;
    @Autowired private InvoicingDetailService invoicingDetailService;
    @Autowired private WorkloadDetailService workloadDetailService;
    @Autowired private PublicHolidayService publicHolidayService;
    @Autowired private PsrService psrService;
    @Autowired private PsrRepository psrRepository;
    @Autowired private PlannedWorkloadMemberService plannedWorkloadMemberService;

    @Override
    public List<DemandeDTO> getAlldemandes() {
        List<Demande> demandes = demandeRepository.findAll();
        return demandes.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public DemandeDTO createDemande(DemandeDTO demandeDTO) {
        Project project = projectRepository.findById(demandeDTO.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // 1️⃣ Récupérer les vrais membres
        List<TeamMember> realMembers = new ArrayList<>();
        if (demandeDTO.getTeamMemberIds() != null) {
            List<Long> realIds = demandeDTO.getTeamMemberIds().stream().filter(id -> id > 0).toList();
            realMembers.addAll(teamMemberRepository.findAllById(realIds));
        }

        // 2️⃣ Mapper les fake members
        List<FakeMember> fakeMembers = demandeDTO.getFakeMembers() != null ?
                demandeDTO.getFakeMembers().stream().map(dto -> {
                    FakeMember fake = new FakeMember();
                    fake.setName(dto.getName());
                    fake.setRole(dto.getRole());
                    fake.setInitial(dto.getInitial());
                    fake.setNote(dto.getNote());
                    return fake;
                }).collect(Collectors.toList()) : new ArrayList<>();

        // 3️⃣ Créer la demande
        Demande demande = new Demande();
        demande.setName(demandeDTO.getName());
        demande.setDateDebut(demandeDTO.getDateDebut());
        demande.setDateFin(demandeDTO.getDateFin());
        demande.setScope(demandeDTO.getScope());
        demande.setRequirements(demandeDTO.getRequirements());
        demande.setProject(project);
        demande.setTeamMembers(realMembers);
        demande.setFakeMembers(fakeMembers);
        demande = demandeRepository.save(demande);

        // 4️⃣ Créer l’équipe générée
        Team generatedTeam = new Team();
        generatedTeam.setName("Team - " + demandeDTO.getName());

        Set<TeamMember> teamMembers = new HashSet<>(realMembers);

        for (FakeMember fake : fakeMembers) {
            TeamMember existingFake = teamMemberRepository
                    .findByFakeTrueAndRole(Seniority.valueOf(fake.getRole()))
                    .orElseThrow(() -> new RuntimeException("Fake member not found for role: " + fake.getRole()));

            teamMembers.add(existingFake);
        }


        generatedTeam.setMembers(teamMembers);
        generatedTeam.getProjects().add(project);
        for (TeamMember member : realMembers) {
            member.getTeams().add(generatedTeam);
        }

        generatedTeam = teamRepository.save(generatedTeam);
        demande.setGeneratedTeam(generatedTeam);
        demande = demandeRepository.save(demande);

        project.getTeams().add(generatedTeam);
        projectRepository.save(project);

        // 5️⃣ Générer le devis
        Devis generatedDevis = createGeneratedDevisForDemande(demande);
        demande.setGeneratedDevis(generatedDevis);
        demande = demandeRepository.save(demande);

        updateProjectDates(project.getId());

        DemandeDTO dto = convertToDTO(demande);
        dto.setGeneratedTeamId(generatedTeam.getId());
        dto.setGeneratedDevisId(generatedDevis.getId());
        return dto;
    }


    private Devis createGeneratedDevisForDemande(Demande demande) {
        if (demande.getId() == null) demande = demandeRepository.save(demande);
        Project project = demande.getProject();
        if (project == null) throw new RuntimeException("❌ La demande doit être liée à un projet.");

        Devis devis = new Devis();
        devis.setReference("Devis" + System.currentTimeMillis());
        devis.setCreationDate(LocalDate.now());
        devis.setProject(project);
        devis.setAuthor(project.getUser().getFirstname());
        devis.setDemande(demande);

        Devis savedDevis = devisRepository.save(devis);

        List<TeamMember> allMembers = new ArrayList<>(demande.getTeamMembers());
        if (demande.getFakeMembers() != null) {
            for (FakeMember f : demande.getFakeMembers()) {
                TeamMember existingFake = teamMemberRepository
                        .findByFakeTrueAndRole(Seniority.valueOf(f.getRole()))
                        .orElseThrow(() -> new RuntimeException("Fake member not found"));
                allMembers.add(existingFake);
            }
        }


        List<FinancialDetail> details = financialDetailService.generateFromTeamMembers(
                allMembers,
                demande.getDateDebut(),
                demande.getDateFin(),
                savedDevis,
                demande
        );

        for (FinancialDetail detail : details) {
            detail.setDevis(savedDevis);
            detail.setDemande(demande);
        }
        financialDetailRepository.saveAll(details);

        LocalDate start = demande.getDateDebut();
        LocalDate end = demande.getDateFin();
        if (start == null || end == null || end.isBefore(start))
            throw new RuntimeException("❌ Dates invalides dans la demande (start: " + start + ", end: " + end + ")");

        List<YearMonth> months = new ArrayList<>();
        YearMonth current = YearMonth.from(start);
        YearMonth endMonth = YearMonth.from(end);
        while (!current.isAfter(endMonth)) {
            months.add(current);
            current = current.plusMonths(1);
        }

        BigDecimal totalCost = details.stream().map(FinancialDetail::getTotalCost).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal monthlyAmount = totalCost.divide(BigDecimal.valueOf(months.size()), 2, RoundingMode.HALF_UP);

        List<InvoicingDetail> invoicingDetails = new ArrayList<>();
        for (YearMonth ym : months) {
            InvoicingDetail invoice = new InvoicingDetail();
            invoice.setDescription(getMonthName(ym.getMonthValue()) + " " + ym.getYear());
            invoice.setInvoicingDate(ym.atEndOfMonth());
            invoice.setAmount(monthlyAmount);
            invoice.setDevis(savedDevis);
            invoice.setDemande(demande);
            invoicingDetails.add(invoice);
        }
        invoicingDetailRepository.saveAll(invoicingDetails);

        workloadDetailService.generateFromDemandes(savedDevis.getId());

        return savedDevis;
    }


    private double estimateCostByRole(Seniority role) {
        return switch (role) {
            case JUNIOR -> 200;
            case INTERMEDIAIRE -> 350;
            case SENIOR -> 500;
            case SENIOR_MANAGER -> 800;
            default -> 300;
        };
    }

    private String getMonthName(int month) {
        return switch (month) {
            case 1 -> "janv";
            case 2 -> "févr";
            case 3 -> "mars";
            case 4 -> "avril";
            case 5 -> "mai";
            case 6 -> "juin";
            case 7 -> "juil";
            case 8 -> "août";
            case 9 -> "sept";
            case 10 -> "oct";
            case 11 -> "nov";
            case 12 -> "déc";
            default -> "inconnu";
        };
    }

    @Override
    public DemandeDTO updateDemande(Long id, DemandeDTO demandeDTO) {
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande not found"));

        // 🔄 Mise à jour des champs de base
        demande.setName(demandeDTO.getName());
        demande.setDateDebut(demandeDTO.getDateDebut());
        demande.setDateFin(demandeDTO.getDateFin());
        demande.setScope(demandeDTO.getScope());
        demande.setRequirements(demandeDTO.getRequirements());

        // 🔄 Lier le projet
        Project project = projectRepository.findById(demandeDTO.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        demande.setProject(project);

        // 🔄 Membres réels
        List<TeamMember> realMembers = new ArrayList<>();
        if (demandeDTO.getTeamMemberIds() != null) {
            List<Long> realIds = demandeDTO.getTeamMemberIds().stream()
                    .filter(memberId -> memberId > 0)
                    .toList();

            realMembers = teamMemberRepository.findAllById(realIds);
            demande.setTeamMembers(realMembers);
        }

        // 🔄 Membres fictifs
        Demande finalDemande = demande;
        List<FakeMember> newFakeMembers = demandeDTO.getFakeMembers() != null ?
                demandeDTO.getFakeMembers().stream().map(dto -> {
                    FakeMember fake = new FakeMember();
                    fake.setName(dto.getName());
                    fake.setRole(dto.getRole());
                    fake.setInitial(dto.getInitial());
                    fake.setNote(dto.getNote());
                    fake.setDemande(finalDemande); // 🔗 Lien inverse
                    return fake;
                }).collect(Collectors.toList()) : new ArrayList<>();

        // ✅ Gestion correcte avec orphanRemoval
        demande.getFakeMembers().clear(); // supprime les anciens (orphanRemoval = true)
        demande.getFakeMembers().addAll(newFakeMembers); // ajoute les nouveaux

        // 💾 Sauvegarde de la demande
        demande = demandeRepository.save(demande);

        // 🔁 Mise à jour de l'équipe générée
        if (demande.getGeneratedTeam() != null) {
            Team team = demande.getGeneratedTeam();
            team.getMembers().clear(); // nettoyer les anciens membres

            Set<TeamMember> updatedMembers = new HashSet<>(realMembers);

            for (FakeMember f : newFakeMembers) {
                TeamMember fake = teamMemberRepository.findByFakeTrueAndRole(Seniority.valueOf(f.getRole()))
                        .orElseThrow(() -> new RuntimeException("Fake member not found: " + f.getRole()));
                updatedMembers.add(fake);
            }

            team.setMembers(updatedMembers);
            teamRepository.save(team);
        }

        // 🔁 Mise à jour du devis généré
        if (demande.getGeneratedDevis() != null) {
            Devis generatedDevis = demande.getGeneratedDevis();
            Long devisId = generatedDevis.getId();

            financialDetailRepository.deleteByDevis_Id(devisId);
            invoicingDetailRepository.deleteByDevis_Id(devisId);
            workloadDetailRepository.deleteAllByDevisId(devisId);

            List<TeamMember> allMembers = new ArrayList<>(realMembers);
            for (FakeMember f : newFakeMembers) {
                TeamMember fake = teamMemberRepository.findByFakeTrueAndRole(Seniority.valueOf(f.getRole()))
                        .orElseThrow(() -> new RuntimeException("Fake member not found for role: " + f.getRole()));
                allMembers.add(fake);
            }

            List<FinancialDetail> details = financialDetailService.generateFromTeamMembers(
                    allMembers, demande.getDateDebut(), demande.getDateFin(), generatedDevis, demande);
            financialDetailRepository.saveAll(details);

            List<InvoicingDetailDTO> dtoList = invoicingDetailService.generateInvoicingDetails(devisId);
            Demande finalDemande1 = demande;
            List<InvoicingDetail> invoices = dtoList.stream().map(dto -> {
                InvoicingDetail i = new InvoicingDetail();
                i.setDescription(dto.getDescription());
                i.setInvoicingDate(dto.getInvoicingDate());
                i.setAmount(dto.getAmount());
                i.setDevis(generatedDevis);
                i.setDemande(finalDemande1);
                return i;
            }).collect(Collectors.toList());
            invoicingDetailRepository.saveAll(invoices);

            workloadDetailService.generateFromDemandes(devisId);
        }

        // 🔁 Mise à jour automatique des dates du projet
        updateProjectDates(project.getId());

        return convertToDTO(demande);
    }


    @Override
    @Transactional
    public void deleteDemande(Long id) {
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande not found"));

        // 1️⃣ Supprimer les planned workloads associés à la demande
        for (TeamMember member : demande.getTeamMembers()) {
            plannedWorkloadMemberService.deleteWorkloadsByProjectAndMember(
                    demande.getProject().getId(),
                    member.getId()
            );
        }
        // 1️⃣ Supprimer les détails du devis généré
        if (demande.getGeneratedDevis() != null) {
            Devis devis = demande.getGeneratedDevis();

            // Supprimer tous les détails liés au devis
            financialDetailRepository.deleteByDevis_Id(devis.getId());
            invoicingDetailRepository.deleteByDevis_Id(devis.getId());
            workloadDetailRepository.deleteAllByDevisId(devis.getId());

            // Supprimer le devis lui-même
            devisRepository.delete(devis);

            // Détacher le lien dans la demande pour éviter violation de contrainte
            demande.setGeneratedDevis(null);
        }

        // 2️⃣ Supprimer la team générée (et les allocations associées)
        if (demande.getGeneratedTeam() != null) {
            Team generatedTeam = demande.getGeneratedTeam();

            // Supprimer les allocations liées à cette team
            List<TeamMemberAllocation> allocations = teamMemberAllocationRepository
                    .findAllByTeamId(generatedTeam.getId());
            teamMemberAllocationRepository.deleteAll(allocations);

            // Nettoyer les relations avec les membres
            generatedTeam.getMembers().forEach(member -> member.getTeams().remove(generatedTeam));
            generatedTeam.getMembers().clear();

            // Nettoyer les relations avec le(s) projet(s)
            for (Project project : new HashSet<>(generatedTeam.getProjects())) {
                project.getTeams().remove(generatedTeam);
            }
            generatedTeam.getProjects().clear();

            // Détacher la team dans la demande
            demande.setGeneratedTeam(null);

            // Supprimer la team
            teamRepository.delete(generatedTeam);
        }

        // 3️⃣ Détacher les membres de la demande
        if (demande.getTeamMembers() != null && !demande.getTeamMembers().isEmpty()) {
            demande.getTeamMembers().clear();
        }

        // 4️⃣ Supprimer la demande
        demandeRepository.delete(demande);

        // 5️⃣ Mettre à jour les dates du projet
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
        List<FakeMemberDTO> fakeMemberDTOs = demande.getFakeMembers() != null
                ? demande.getFakeMembers().stream().map(f ->
                new FakeMemberDTO(f.getName(), f.getRole(), f.getInitial(), f.getNote())
        ).collect(Collectors.toList())
                : new ArrayList<>();

        return DemandeDTO.builder()
                .id(demande.getId())
                .name(demande.getName())
                .dateDebut(demande.getDateDebut())
                .dateFin(demande.getDateFin())
                .projectId(demande.getProject() != null ? demande.getProject().getId() : null)
                .projectName(demande.getProject() != null ? demande.getProject().getName() : "Projet inconnu")
                .teamMemberIds(demande.getTeamMembers() != null
                        ? demande.getTeamMembers().stream().map(TeamMember::getId).collect(Collectors.toSet())
                        : new HashSet<>())
                .scope(demande.getScope())
                .requirements(demande.getRequirements())
                .generatedTeamId(demande.getGeneratedTeam() != null ? demande.getGeneratedTeam().getId() : null)
                .generatedDevisId(demande.getGeneratedDevis() != null ? demande.getGeneratedDevis().getId() : null)
                .fakeMembers(fakeMemberDTOs)
                .build();
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
