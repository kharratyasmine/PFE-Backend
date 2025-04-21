package com.workpilot.service.GestionProject.client;

import com.workpilot.dto.ClientDTO;
import com.workpilot.dto.ProjectDTO;
import com.workpilot.entity.ressources.Client;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

public interface ClientService {

    @Transactional
    Client saveClient(ClientDTO dto); // ✅ Ajout de @Transactional pour assurer la cohérence des données

    @Transactional
    Client updateClient(Long id, ClientDTO clientDTO);  // ✅ DTO utilisé pour éviter l'exposition directe des entités

    Optional<Client> getClientById(Long id);  // ✅ Retourne Optional<Client> pour éviter les erreurs null

    @Transactional
    void deleteClient(Long id);  // ✅ Suppression sécurisée avec transaction

    List<ProjectDTO> getProjectsByClient(Long clientId);  // ✅ Retourne une liste de ProjectDTO pour respecter le pattern DTO

    List<ClientDTO> getAllClients();  // ✅ Assure la cohérence en retournant une liste de DTOs
}
