package com.workpilot.service;

import com.workpilot.entity.Client;
import com.workpilot.entity.Project;

import java.util.List;

public interface ClientService {
    List<Client> getAllClients();  // Correction : nom en camelCase
    Client saveClient(Client client);
    Client updateClient(Long id, Client newClientData); // 🔹 Mise à jour avec liste de projets
    Client getClientById(Long id);
    void deleteClient(Long id);
    List<Project> getProjectsByClient(Long id);
}
