package com.workpilot.service;

import com.workpilot.entity.Client;
import com.workpilot.entity.Project;
import com.workpilot.repository.ClientRepository;
import com.workpilot.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Override
    public List<Client> getAllClients() {
        List<Client> clients = clientRepository.findAll();
        // ✅ Vérifier la structure des objets renvoyés
        for (Client client : clients) {
            System.out.println("🔍 Client : " + client.getName() + " - Projets associés : " + client.getProjects().size());
        }
        return clients;
    }

    // ✅ Ajouter un client avec plusieurs projets
    public Client saveClient(Client client) {
        System.out.println("📤 Tentative de sauvegarde du client : " + client);

        if (client.getId() != null && client.getId() == 0) {
            client.setId(null); // ✅ Pour éviter le conflit de transaction avec Hibernate
        }

        return clientRepository.save(client);
    }


    @Override
    public Client updateClient(Long id, Client newClientData) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client introuvable avec ID: " + id));

        client.setName(newClientData.getName());
        client.setContact(newClientData.getContact());
        client.setAddress(newClientData.getAddress());
        client.setEmail(newClientData.getEmail());
               return clientRepository.save(client);
    }


    @Override
    public Client getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client avec ID " + id + " introuvable."));
    }

    @Override
    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new RuntimeException("Client avec ID " + id + " introuvable.");
        }
        clientRepository.deleteById(id);
    }


    public List<Project> getProjectsByClient(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        return client.getProjects();
    }
}
