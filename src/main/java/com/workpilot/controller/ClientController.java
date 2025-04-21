package com.workpilot.controller;

import com.workpilot.dto.ClientDTO;
import com.workpilot.dto.ProjectDTO;
import com.workpilot.entity.ressources.Client;
import com.workpilot.service.GestionProject.client.ClientService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
@CrossOrigin(origins = "http://localhost:4200")
public class ClientController {

    @Autowired
    private ClientService clientService;

    // Récupérer tous les clients
    @GetMapping
    public ResponseEntity<List<ClientDTO>> getAllClients() {
        List<ClientDTO> clientDTOs = clientService.getAllClients();
        return ResponseEntity.ok(clientDTOs); // ✅ Renvoie une liste propre
    }

    // Création client avec DTO
    @PostMapping
    public ResponseEntity<?> createClient(@Valid @RequestBody ClientDTO clientDTO) {
        try {
            Client savedClient = clientService.saveClient(clientDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedClient);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'ajout : " + e.getMessage());
        }
    }



    // Mise à jour client avec DTO
    @PutMapping("/{id}")
    public ResponseEntity<?> updateClient(@PathVariable Long id, @Valid @RequestBody ClientDTO clientDTO) {
        try {
            Client updatedClient = clientService.updateClient(id, clientDTO);
            return ResponseEntity.ok(updatedClient); // ✅ Retourne le client mis à jour
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // ✅ Retourne une erreur 404 si client non trouvé
        }
    }

    // Supprimer un client
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        try {
            clientService.deleteClient(id);
            return ResponseEntity.noContent().build(); // ✅ Pas de contenu à retourner après suppression
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build(); // ✅ Retourne 404 si client non trouvé
        }
    }

    @GetMapping("/{id}/projects")
    public ResponseEntity<List<ProjectDTO>> getProjectsByClient(@PathVariable Long id) {
        try {
            List<ProjectDTO> projects = clientService.getProjectsByClient(id);
            return ResponseEntity.ok(projects); // ✅ Retourne les projets du client sous forme de DTO
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build(); // ✅ Retourne 404 si aucun projet trouvé pour ce client
        }
    }


    // Méthode utilitaire privée pour conversion DTO -> Entity
    private Client convertToEntity(ClientDTO dto) {
        Client client = new Client();
        client.setCompany(dto.getCompany());
        client.setSalesManagers(dto.getSalesManagers());
        client.setContact(dto.getContact());
        client.setAddress(dto.getAddress());
        client.setEmail(dto.getEmail());
        return client;
    }
}
