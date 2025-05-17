package com.workpilot.controller.PSRController;

import com.workpilot.dto.PsrDTO.DeliveriesDTO;
import com.workpilot.entity.PSR.Psr;
import com.workpilot.repository.Psr.DeliveriesRepository;
import com.workpilot.repository.Psr.PsrRepository;
import com.workpilot.service.PSR.Deliveries.DeliveriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deliveries")
@CrossOrigin(origins = "http://localhost:4200")
public class DeliveriesController {

    @Autowired
    private DeliveriesService deliveriesService;

    @Autowired
    private PsrRepository psrRepository;

    @Autowired
    private DeliveriesRepository deliveriesRepository;

    @PostMapping("/psr/{psrId}")
    public ResponseEntity<DeliveriesDTO> addDelivery(@PathVariable Long psrId, @RequestBody DeliveriesDTO dto) {
        return ResponseEntity.ok(deliveriesService.addDeliveryToPsr(psrId, dto));
    }


    @DeleteMapping("/{deliveryId}")
    public ResponseEntity<Void> deleteDelivery(@PathVariable Long deliveryId) {
        deliveriesService.deleteDelivery(deliveryId);
        return ResponseEntity.noContent().build();
    }

    // Endpoint to get all histories for a specific Devis
    @GetMapping("/psr/{psrId}/deliveries")
    public ResponseEntity<List<DeliveriesDTO>> getDeliveriesByPsrId(@PathVariable Long psrId) {
        return ResponseEntity.ok(deliveriesService.getDeliveriesByPsrId(psrId));
    }



}
