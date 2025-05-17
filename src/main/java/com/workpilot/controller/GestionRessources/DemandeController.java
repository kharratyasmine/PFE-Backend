package com.workpilot.controller.GestionRessources;

import com.workpilot.dto.GestionRessources.DemandeDTO;
import com.workpilot.service.GestionRessources.demande.DemandeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/demandes")
@CrossOrigin(origins = "http://localhost:4200")
public class DemandeController {
    private final DemandeService demandeService;

    public DemandeController(DemandeService demandeService) {
        this.demandeService = demandeService;
    }

    // Récupérer tous les clients
    @GetMapping
    public ResponseEntity<List<DemandeDTO>> getAllDemands() {
        List<DemandeDTO> demandeDTOs = demandeService.getAlldemandes();
        return ResponseEntity.ok(demandeDTOs);
    }

    @PostMapping
    public ResponseEntity<DemandeDTO> createDemande(@RequestBody DemandeDTO demande) {
        try {
            DemandeDTO createdDemande = demandeService.createDemande(demande);
            return new ResponseEntity<>(createdDemande, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<DemandeDTO>> getDemandesByProject(@PathVariable Long projectId) {
        List<DemandeDTO> demandes = demandeService.getDemandesByProject(projectId);

        if (demandes.isEmpty()) {
            return ResponseEntity.ok(demandes); // renvoie []
        }
        return ResponseEntity.ok(demandes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DemandeDTO> updateDemande(@PathVariable Long id, @RequestBody DemandeDTO demandeDTO) {
        DemandeDTO updated = demandeService.updateDemande(id, demandeDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDemande(@PathVariable Long id) {
        demandeService.deleteDemande(id);
        return ResponseEntity.noContent().build();
    }
}