package com.workpilot.controller.DevisController;

import com.workpilot.dto.DevisDTO.DevisDTO;
import com.workpilot.service.DevisServices.devis.DevisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/devis")
@CrossOrigin(origins = "http://localhost:4200")
public class DevisController {

    private final DevisService devisService;

    public DevisController(DevisService devisService) {
        this.devisService = devisService;
    }

    @GetMapping
    public List<DevisDTO> getAllDevis() {
        return devisService.getAllDevis();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DevisDTO> getDevisById(@PathVariable Long id) {
        Optional<DevisDTO> devis = devisService.getDevisById(id);
        return devis.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public DevisDTO createDevis(@RequestBody DevisDTO devisDTO) {
        return devisService.createDevis(devisDTO);
    }

    // ✅ NOUVELLE MÉTHODE POUR METTRE À JOUR UN DEVIS
    @PutMapping("/{id}")
    public ResponseEntity<DevisDTO> updateDevis(@PathVariable Long id, @RequestBody DevisDTO devisDTO) {
        DevisDTO updatedDevis = devisService.updateDevis(id, devisDTO);
        return ResponseEntity.ok(updatedDevis);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevis(@PathVariable Long id) {
        devisService.deleteDevis(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/project/{projectId}")
    public List<DevisDTO> getDevisByProjectId(@PathVariable Long projectId) {
        return devisService.getDevisByProjectId(projectId);
    }



}
