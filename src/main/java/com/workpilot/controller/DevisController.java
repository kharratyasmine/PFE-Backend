package com.workpilot.controller;


import com.workpilot.entity.Devis;
import com.workpilot.exception.DevisNotFoundException;
import com.workpilot.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devis")
@CrossOrigin("*")
public class DevisController {
   @Autowired
    private final DevisService devisService;

    @Autowired
    private PdfService pdfService;

    @GetMapping("/{id}/export/pdf")
    public ResponseEntity<byte[]> exportDevisPdf(@PathVariable Long id) {
        Devis devis = devisService.getDevisById(id);
        byte[] pdfBytes = pdfService.generateDevisPdf(devis);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=devis_" + id + ".pdf")
                .body(pdfBytes);
    }
    @Autowired
    public DevisController(DevisService devisService) {
        this.devisService = devisService;
    }

    @GetMapping
    public ResponseEntity<List<Devis>> getAllDevis() {
        List<Devis> devis = devisService.getAllDevis();
        return ResponseEntity.ok(devis);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Devis> getDevisById(@PathVariable Long id) {
        Devis devis = devisService.getDevisById(id);
        return devis != null ? ResponseEntity.ok(devis) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Devis> createDevis(@RequestBody Devis devis) {
        Devis createdDevis = devisService.createDevis(devis);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDevis);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Devis> updateDevis(@PathVariable Long id, @RequestBody Devis updatedDevis) {
        Devis updated = devisService.updateDevis(id, updatedDevis);
        return ResponseEntity.ok(updated);
    }




    @DeleteMapping("/{id}")
    public String deleteidDevis(@PathVariable("id") Long id) {
        devisService.deleteDevis (id);
        return "Deleted Successfully";
    }
}
