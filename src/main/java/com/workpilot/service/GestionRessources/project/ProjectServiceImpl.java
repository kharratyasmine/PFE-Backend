package com.workpilot.service.GestionRessources.project;

import com.workpilot.dto.GestionRessources.*;
import com.workpilot.entity.PSR.TeamOrganization;
import com.workpilot.entity.devis.Devis;
import com.workpilot.entity.ressources.*;
import com.workpilot.repository.Psr.TeamOrganizationRepository;
import com.workpilot.repository.devis.DevisRepository;
import com.workpilot.repository.devis.FinancialDetailRepository;
import com.workpilot.repository.devis.InvoicingDetailRepository;
import com.workpilot.repository.devis.WorkloadDetailRepository;
import com.workpilot.repository.ressources.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    private TeamOrganizationRepository teamOrganizationRepository;

    @Autowired
    private TeamMemberAllocationRepository teamMemberAllocationRepository;

    @Autowired
    private DemandeRepository demandeRepository;

    @Autowired
    private WorkloadDetailRepository workloadDetailRepository;

    @Autowired
    private WorkEntryRepository workEntryRepository;

    @Autowired
    private DevisRepository devisRepository;
    @Autowired
    private FinancialDetailRepository financialDetailRepository;

    @Autowired
    private InvoicingDetailRepository invoicingDetailRepository;

    @Autowired
    private PlannedWorkloadMemberRepository plannedWorkloadMemberRepository;
    @Override
    public Project createProject(ProjectDTO dto) {
        Project project = new Project();
        setProjectFields(project, dto);
        return projectRepository.save(project);
    }

    private void setProjectFields(Project project, ProjectDTO dto) {
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setProjectType(dto.getProjectType());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        // Définir le statut automatiquement
        if (project.getId() == null) {
            // Création : statut par défaut
            project.setStatus(Status.EN_COURS);
        } else {
            // Mise à jour : appliquer une logique si nécessaire
            if (project.getEndDate() != null && project.getEndDate().isBefore(LocalDate.now())) {
                project.setStatus(Status.TERMINE);
            }
            // Sinon, garder le statut existant
        }
        project.setActivity(dto.getActivity());
        project.setTechnologie(dto.getTechnologie());

        if (dto.getClientId() != null) {
            project.setClient(
                    clientRepository.findById(dto.getClientId())
                            .orElseThrow(() -> new EntityNotFoundException("Client introuvable"))
            );
        }

        if (dto.getUserId() != null) {
            project.setUser(
                    userRepository.findById(dto.getUserId())
                            .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable"))
            );
        }
        project.setTeams(new HashSet<>()); // Lien automatique via Demande uniquement

    }

    @Override
    public List<ProjectDTO> GetAllProject() {
        return projectRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectDTO updateProject(Long id, ProjectDTO dto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        setProjectFields(project, dto);
        project = projectRepository.save(project);


        // 🔄 Mettre à jour les PlannedStartDate et PlannedEndDate dans TeamOrganization
        List<TeamOrganization> teamOrgs = teamOrganizationRepository.findByPsrProjectId(project.getId());
        for (TeamOrganization org : teamOrgs) {
            org.setPlannedStartDate(project.getStartDate());
            org.setPlannedEndDate(project.getEndDate());
        }
        teamOrganizationRepository.saveAll(teamOrgs);
        return convertToDTO(project);
    }

    @Override
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Projet introuvable : " + id));
    }



    @Transactional
    public void deleteProject(Long id) {
        /*try {
            Project project = projectRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projet non trouvé"));

            System.out.println("Début de la suppression du projet: " + id);

            // 1. Désactiver temporairement les contraintes de clé étrangère
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

            // 2. Supprimer les entrées de travail
            workEntryRepository.deleteByProjectId(id);
            System.out.println("✅ Entrées de travail supprimées");

            // 3. Supprimer les tâches
            projectTaskRepository.deleteByProjectId(id);
            System.out.println("✅ Tâches supprimées");

            // 4. Supprimer les allocations
            teamMemberAllocationRepository.deleteByProjectId(id);
            System.out.println("✅ Allocations supprimées");

            // 5. Supprimer les plannings
            plannedWorkloadMemberRepository.deleteByProjectId(id);
            System.out.println("✅ Plannings supprimés");

            // 6. Supprimer les organisations d'équipe PSR
            teamOrganizationRepository.deleteByPsrProjectId(id);
            System.out.println("✅ Organisations d'équipe supprimées");

            // 7. Gérer les devis et leurs détails (nouvelle approche)
            deleteAllDevisForProject(id);

            // 8. Supprimer les demandes
            demandeRepository.deleteByProjectId(id);
            System.out.println("✅ Demandes supprimées");

            // 9. Gérer les relations avec les équipes
            for (Team team : project.getTeams()) {
                team.getProjects().remove(project);
                teamRepository.save(team);
            }
            System.out.println("✅ Relations avec les équipes supprimées");

            // 10. Réactiver les contraintes
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();

            // 11. Supprimer le projet
            projectRepository.delete(project);
            System.out.println("✅ Projet supprimé avec succès");

        } catch (Exception e) {
            // Réactiver les contraintes en cas d'erreur
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
            System.err.println("❌ Erreur lors de la suppression du projet: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors de la suppression du projet: " + e.getMessage());
        }
    }

    private void deleteAllDevisForProject(Long projectId) {
        List<Devis> devisList = devisRepository.findByProjectId(projectId); // sans fetch multiple

        for (Devis devis : devisList) {
            Long devisId = devis.getId();

            entityManager.createNativeQuery("DELETE FROM workload_detail WHERE devis_id = ?")
                    .setParameter(1, devisId).executeUpdate();

            entityManager.createNativeQuery("DELETE FROM financial_detail WHERE devis_id = ?")
                    .setParameter(1, devisId).executeUpdate();

            entityManager.createNativeQuery("DELETE FROM invoicing_detail WHERE devis_id = ?")
                    .setParameter(1, devisId).executeUpdate();

            entityManager.createNativeQuery("DELETE FROM devis WHERE id = ?")
                    .setParameter(1, devisId)
                    .executeUpdate();

        }*/
    }

    public ProjectDTO convertToDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setProjectType(project.getProjectType());
        dto.setDescription(project.getDescription());
        dto.setStartDate(project.getStartDate());
        dto.setEndDate(project.getEndDate());
        dto.setStatus(project.getStatus());
        dto.setActivity(project.getActivity());
        dto.setTechnologie(project.getTechnologie());
        dto.setClientId(project.getClient() != null ? project.getClient().getId() : null);
        dto.setUserId(project.getUser() != null ? project.getUser().getId() : null);
        dto.setUserName(project.getUser() != null ? project.getUser().getFirstname() : "Aucun utilisateur");

        dto.setTeams(
                project.getTeams() != null
                        ? project.getTeams().stream()
                        .map(team -> convertToTeamDTO(team, project.getId()))
                        .collect(Collectors.toList())
                        : new ArrayList<>()
        );

        dto.setDemandes(
                project.getDemandes() != null
                        ? project.getDemandes().stream()
                        .map(demande -> new DemandeDTO(
                                demande.getId(),
                                demande.getName(),
                                demande.getDateDebut(),
                                demande.getDateFin(),
                                demande.getProject() != null ? demande.getProject().getId() : null,
                                demande.getProject() != null ? demande.getProject().getName() : "Aucun projet",
                                demande.getTeamMembers() != null
                                        ? demande.getTeamMembers().stream().map(TeamMember::getId).collect(Collectors.toSet())
                                        : new HashSet<>(),
                                demande.getRequirements(),
                                demande.getScope(),
                                null,
                                null,
                                demande.getFakeMembers() != null
                                        ? demande.getFakeMembers().stream()
                                        .map(fm -> new FakeMemberDTO(fm.getName(), fm.getRole(), fm.getInitial(), fm.getNote()))
                                        .collect(Collectors.toList())
                                        : new ArrayList<>()
                        ))
                        .collect(Collectors.toList())
                        : new ArrayList<>()
        );
        if (project.getUser() != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(project.getUser().getId());
            userDTO.setFirstname(project.getUser().getFirstname());
            userDTO.setLastname(project.getUser().getLastname());
            userDTO.setEmail(project.getUser().getEmail());
            userDTO.setPhoneNumber(project.getUser().getPhoneNumber());
            userDTO.setAddress(project.getUser().getAddress());
            userDTO.setPhotoUrl(project.getUser().getPhotoUrl());
            userDTO.setRole(project.getUser().getRole());
            dto.setUser(userDTO); // ✅ injection dans le ProjectDTO
        }
        if (project.getClient() != null) {
            ClientDTO clientDTO = new ClientDTO();
            clientDTO.setId(project.getClient().getId());
            clientDTO.setCompany(project.getClient().getCompany());
            clientDTO.setSalesManagers(project.getClient().getSalesManagers());
            clientDTO.setContact(project.getClient().getContact());
            clientDTO.setAddress(project.getClient().getAddress());
            clientDTO.setEmail(project.getClient().getEmail());
            dto.setClient(clientDTO);
        }




        return dto;
    }

    private TeamDTO convertToTeamDTO(Team team, Long projectId) {
        TeamDTO dto = new TeamDTO();
        dto.setId(team.getId());
        dto.setName(team.getName());

        if (team.getProjects() != null) {
            dto.setProjectIds(
                    team.getProjects().stream()
                            .map(Project::getId)
                            .collect(Collectors.toSet())
            );
        }

        if (team.getMembers() != null) {
            dto.setMembers(
                    team.getMembers().stream()
                            .map(member -> convertToMemberDTO(member, projectId,team))
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }

    private TeamMemberDTO convertToMemberDTO(TeamMember member, Long projectId, Team team) {
        double allocation = teamMemberAllocationRepository
                .findAllByTeamMemberIdAndProjectIdAndTeamId(member.getId(), projectId ,team.getId())
                .stream()
                .mapToDouble(TeamMemberAllocation::getAllocation)
                .sum();

        List<Long> teamIds = member.getTeams() != null
                ? member.getTeams().stream().map(Team::getId).collect(Collectors.toList())
                : new ArrayList<>();

        return new TeamMemberDTO(
                member.getId(),
                member.getName(),
                member.getInitial(),
                member.getJobTitle(),
                member.getHoliday(),
                member.getRole(),
                member.getCost(),
                member.getNote(),
                member.getImage(),
                teamIds,
                member.getExperienceRange(),
                allocation
        );
    }

    @Override
    @Transactional
    public void assignTeamToProject(Long projectId, Long teamId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Projet introuvable"));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Équipe introuvable"));

        project.getTeams().add(team);
        projectRepository.save(project);
    }

    @Override
    public List<TeamMemberAllocationDTO> getAllocationsByProjectId(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        return project.getAllocations().stream().map(allocation -> {
            TeamMemberAllocationDTO dto = new TeamMemberAllocationDTO();
            TeamMember member = allocation.getTeamMember();

            dto.setMemberId(member.getId());
            dto.setAllocation(allocation.getAllocation());

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<TeamAllocationDTO> getTeamAllocationsByProjectId(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        return project.getTeams().stream().map(team -> {
            TeamAllocationDTO teamDto = new TeamAllocationDTO();
            teamDto.setTeamId(team.getId());
            teamDto.setTeamName(team.getName());

            List<TeamMemberAllocationDTO> memberAllocations = team.getMembers().stream().map(member -> {
                TeamMemberAllocationDTO memberDto = new TeamMemberAllocationDTO();
                memberDto.setMemberId(member.getId());

                Optional<TeamMemberAllocation> allocationOpt = project.getAllocations().stream()
                        .filter(alloc -> alloc.getTeamMember().getId().equals(member.getId()))
                        .findFirst();

                memberDto.setAllocation(
                        allocationOpt.map(TeamMemberAllocation::getAllocation).orElse(0.0)
                );

                return memberDto;
            }).collect(Collectors.toList());

            // Ajout des fake members liés à cette team via les demandes
            List<Demande> demandes = project.getDemandes();
            if (demandes != null) {
                for (Demande demande : demandes) {
                    if (demande.getGeneratedTeam() != null && demande.getGeneratedTeam().getId().equals(team.getId())) {
                        if (demande.getFakeMembers() != null) {
                            for (FakeMember fake : demande.getFakeMembers()) {
                                TeamMemberAllocationDTO fakeDto = new TeamMemberAllocationDTO();
                                fakeDto.setMemberId(-1L); // ou un hash négatif unique si besoin
                                fakeDto.setAllocation(0.0);
                                memberAllocations.add(fakeDto);
                            }
                        }
                    }
                }
            }

            teamDto.setMembers(memberAllocations);
            return teamDto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<TeamMemberDTO> getMembersByProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Projet non trouvé"));

        return project.getTeams().stream()
                .flatMap(team -> team.getMembers().stream()
                        .map(member -> convertToMemberDTO(member, projectId, team)))
                .distinct() // facultatif selon l'effet désiré
                .collect(Collectors.toList());
    }

    @Override
    public void removeTeamFromProject(Long projectId, Long teamId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Équipe non trouvée"));

        project.getTeams().remove(team);
        projectRepository.save(project); // ✅ Mettre à jour la BDD
    }

}
