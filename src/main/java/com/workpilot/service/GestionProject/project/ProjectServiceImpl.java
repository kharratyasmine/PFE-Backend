package com.workpilot.service.GestionProject.project;

import com.workpilot.dto.*;
import com.workpilot.entity.ressources.*;
import com.workpilot.repository.*;
import jakarta.persistence.EntityNotFoundException;
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
    private TeamMemberAllocationRepository teamMemberAllocationRepository;

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

        if (dto.getTeamIds() != null && !dto.getTeamIds().isEmpty()) {
            Set<Team> teams = dto.getTeamIds().stream()
                    .map(id -> teamRepository.findById(id)
                            .orElseThrow(() -> new EntityNotFoundException("Équipe introuvable avec l'ID : " + id)))
                    .collect(Collectors.toSet());
            project.setTeams(teams);
        } else {
            project.setTeams(new HashSet<>());
        }
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
        return convertToDTO(project);
    }

    @Override
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Projet introuvable : " + id));
    }

    @Transactional
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projet non trouvé"));

        projectTaskRepository.deleteByProjectId(id);
        projectRepository.delete(project);
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
                                null
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
                            .map(member -> convertToMemberDTO(member, projectId))
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }

    private TeamMemberDTO convertToMemberDTO(TeamMember member, Long projectId) {
        double allocation = teamMemberAllocationRepository
                .findAllByTeamMemberIdAndProjectId(member.getId(), projectId)
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

            teamDto.setMembers(memberAllocations);
            return teamDto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<TeamMemberDTO> getMembersByProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Projet non trouvé"));

        return project.getTeams().stream()
                .flatMap(team -> team.getMembers().stream())
                .distinct()
                .map(member -> convertToMemberDTO(member, projectId))
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
