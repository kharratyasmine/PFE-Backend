package com.workpilot.service.GestionRessources.client;

import com.workpilot.dto.DevisDTO.DevisDTO;
import com.workpilot.dto.GestionRessources.*;
import com.workpilot.entity.ressources.Client;
import com.workpilot.entity.ressources.Project;
import com.workpilot.entity.ressources.TeamMember;
import com.workpilot.repository.ressources.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClientServiceImpl implements ClientService {

    private static final Logger logger = LoggerFactory.getLogger(ClientServiceImpl.class);

    @Autowired
    private ClientRepository clientRepository;

    @Override
    public List<ClientDTO> getAllClients() {
        List<Client> clients = clientRepository.findAll();
        return clients.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private ClientDTO convertToDTO(Client client) {
        ClientDTO dto = new ClientDTO();
        dto.setId(client.getId());
        dto.setCompany(client.getCompany());
        dto.setSalesManagers(client.getSalesManagers());
        dto.setContact(client.getContact());
        dto.setAddress(client.getAddress());
        dto.setEmail(client.getEmail());

        // Conversion des projets en DTO
        List<ProjectDTO> projectDTOs = client.getProjects().stream()
                .map(this::convertToProjectDTO)
                .collect(Collectors.toList());
        dto.setProjects(projectDTOs);

        return dto;
    }

    private ProjectDTO convertToProjectDTO(Project project) {
        return new ProjectDTO(
                project.getId(),
                project.getName(),
                project.getProjectType(),
                project.getDescription(),
                project.getStartDate(),
                project.getEndDate(),
                project.getActivity(),
                project.getTechnologie(),
                project.getStatus(),

                // ✅ Client complet (remplace clientId et clientName)
                project.getClient() != null ? convertToClientDTO(project.getClient()) : null,

                // ✅ User (ID et nom)
                project.getUser() != null ? project.getUser().getId() : null,
                project.getUser() != null ? project.getUser().getFirstname() : null,

                // ✅ Teams
                project.getTeams() != null
                        ? project.getTeams().stream()
                        .map(team -> new TeamDTO(
                                team.getId(),
                                team.getName(),
                                team.getProjects() != null
                                        ? team.getProjects().stream()
                                        .map(Project::getId)
                                        .collect(Collectors.toSet())
                                        : new HashSet<>()
                        ))
                        .collect(Collectors.toList())
                        : new ArrayList<>(),

                project.getDemandes() != null
                        ? project.getDemandes().stream()
                        .map(demande -> new DemandeDTO(
                                demande.getId(),
                                demande.getName(),
                                demande.getDateDebut(),
                                demande.getDateFin(),
                                demande.getProject() != null ? demande.getProject().getId() : null,
                                demande.getProject() != null ? demande.getProject().getName() : null,
                                demande.getTeamMembers() != null
                                        ? demande.getTeamMembers().stream().map(TeamMember::getId).collect(Collectors.toSet())
                                        : new HashSet<>(),
                                demande.getScope(),
                                demande.getRequirements(),
                                demande.getGeneratedTeam() != null ? demande.getGeneratedTeam().getId() : null,
                                demande.getGeneratedDevis() != null ? demande.getGeneratedDevis().getId() : null,
                                demande.getFakeMembers() != null
                                        ? demande.getFakeMembers().stream()
                                        .map(fm -> new FakeMemberDTO(fm.getName(), fm.getRole(), fm.getInitial(), fm.getNote()))
                                        .collect(Collectors.toList())
                                        : new ArrayList<>()
                        ))
                        .collect(Collectors.toList())
                        : new ArrayList<>(),


                // ✅ Devis
                project.getDevisList() != null
                        ? project.getDevisList().stream()
                        .map(devis -> new DevisDTO(devis.getId(), devis.getReference(), devis.getAuthor()))
                        .collect(Collectors.toList())
                        : new ArrayList<>()
        );
    }

    private ClientDTO convertToClientDTO(Client client) {
        ClientDTO dto = new ClientDTO();
        dto.setId(client.getId());
        dto.setCompany(client.getCompany());
        dto.setSalesManagers(client.getSalesManagers());
        dto.setEmail(client.getEmail());
        dto.setAddress(client.getAddress());
        dto.setContact(client.getContact());
        dto.setSalesManagers(client.getSalesManagers());
        return dto;
    }

    private Set<Long> convertTeamMembersToIds(List<TeamMember> teamMembers) {
        return teamMembers == null
                ? Collections.emptySet()
                : teamMembers.stream()
                .map(TeamMember::getId)
                .collect(Collectors.toSet());
    }


    @Override
    @Transactional
    public Client saveClient(ClientDTO dto) {
        Client client = new Client();
        client.setCompany(dto.getCompany());
        client.setSalesManagers(dto.getSalesManagers() != null ? dto.getSalesManagers() : new ArrayList<>());
        client.setContact(dto.getContact());
        client.setAddress(dto.getAddress());
        client.setEmail(dto.getEmail());

        logger.info("📤 Sauvegarde du client : {}", client.getSalesManagers());
        return clientRepository.save(client);
    }


    @Override
    @Transactional
    public Client updateClient(Long id, ClientDTO dto) {
        Client existingClient = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("❌ Client introuvable : " + id));

        existingClient.setCompany(dto.getCompany());
        existingClient.setSalesManagers(dto.getSalesManagers());
        existingClient.setContact(dto.getContact());
        existingClient.setAddress(dto.getAddress());
        existingClient.setEmail(dto.getEmail());

        logger.info("🔄 Mise à jour du client : {}", existingClient.getSalesManagers());
        return clientRepository.save(existingClient);
    }

    @Override
    public Optional<Client> getClientById(Long id) {
        return clientRepository.findById(id); // ✅ Retourne un Optional directement
    }


    @Override
    @Transactional
    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("❌ Client introuvable : " + id));

        clientRepository.delete(client);
        logger.info("🗑️ Client supprimé : ID {}", id);
    }

    @Override
    public List<ProjectDTO> getProjectsByClient(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("❌ Client introuvable : " + clientId));

        return client.getProjects().stream()
                .map(this::convertToProjectDTO)
                .collect(Collectors.toList());
    }

}
