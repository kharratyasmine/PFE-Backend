package com.workpilot.controller;

import com.workpilot.entity.Client;
import com.workpilot.entity.Project;
import com.workpilot.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Client>> getAllClients() {
        List<Client> clients = clientService.getAllClients();
        return ResponseEntity.ok().body(clients);
    }
    @PostMapping
    public ResponseEntity<Client> createClient(@RequestBody Client client) {
        System.out.println("📥 Requête reçue pour ajout : " + client);
        Client savedClient = clientService.saveClient(client);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedClient);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable Long id, @RequestBody Client client) {
        System.out.println("📥 Données reçues pour mise à jour : " + client);

        Optional<Client> existingClient = Optional.ofNullable(clientService.getClientById(id));
        if (existingClient.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        client.setId(id);
        Client updatedClient = clientService.saveClient(client);
        return ResponseEntity.ok(updatedClient);
    }



    @GetMapping("/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }



    @GetMapping("/{id}/projects")
    public ResponseEntity<List<Project>> getProjectsByClient(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getProjectsByClient(id));
    }

}
