package com.workpilot.controller.DevisController;

import com.workpilot.dto.DevisDTO.VisaDTO;

import com.workpilot.service.DevisServices.visa.VisaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/visa")
@CrossOrigin(origins = "http://localhost:4200")
public class VisaController {

    @Autowired
    private VisaService visaService;

    @GetMapping("/devis/{devisId}")
    public List<VisaDTO> getVisasByDevisId(@PathVariable Long devisId) {
        return visaService.getVisasByDevisId(devisId);
    }

    @PostMapping("/devis/{devisId}")
    public VisaDTO addVisa(@PathVariable Long devisId, @RequestBody VisaDTO visaDTO) {
        return visaService.addVisa(devisId, visaDTO);
    }

    @PutMapping("/{id}")
    public VisaDTO updateVisa(@PathVariable Long id, @RequestBody VisaDTO visaDTO) {
        return visaService.updateVisa(id, visaDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteVisa(@PathVariable Long id) {
        visaService.deleteVisa(id);
    }
}
