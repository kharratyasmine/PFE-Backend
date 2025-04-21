package com.workpilot.controller.DevisController;

import com.workpilot.dto.DevisDTO.InvoicingDetailDTO;
import com.workpilot.entity.devis.InvoicingDetail;
import com.workpilot.service.DevisServices.InvoicingDetail.InvoicingDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invoicingDetails")
@CrossOrigin(origins = "http://localhost:4200")
public class InvoicingDetailController {

    @Autowired
    private InvoicingDetailService invoicingDetailService;

    @GetMapping
    public ResponseEntity<List<InvoicingDetail>> GetAllInvoicingDetail() {
        List<InvoicingDetail> invoicingDetail = invoicingDetailService.GetAllInvoicingDetail();
        return ResponseEntity.ok(invoicingDetail);

    }
    @PostMapping
    public ResponseEntity<InvoicingDetail> createInvoicingDetail(@RequestBody InvoicingDetail invoicingDetail) {
        return ResponseEntity.ok(invoicingDetailService.createInvoicingDetail(invoicingDetail));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoicingDetail> updateInvoicingDetail(@PathVariable Long id, @RequestBody InvoicingDetail invoicingDetail) {
        return ResponseEntity.ok(invoicingDetailService.updateInvoicingDetail(id, invoicingDetail));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoicingDetail> getInvoicingDetailById(@PathVariable Long id) {
        return ResponseEntity.ok(invoicingDetailService.getInvoicingDetailById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoicingDetail(@PathVariable Long id) {
        invoicingDetailService.deleteInvoicingDetail(id);
        return ResponseEntity.noContent().build();
    }

    // Récupérer les InvoicingDetail associés à un devis
    @GetMapping("/devis/{devisId}")
    public ResponseEntity<List<InvoicingDetail>> getByDevis(@PathVariable Long devisId) {
        List<InvoicingDetail> details = invoicingDetailService.getByDevisId(devisId);
        return ResponseEntity.ok(details);
    }

    @PostMapping("/generate/{devisId}/{startMonth}")
    public ResponseEntity<List<InvoicingDetailDTO>> generateInvoicingDetails(
            @PathVariable Long devisId,
            @PathVariable int startMonth
    ) {
        List<InvoicingDetailDTO> generated = invoicingDetailService.generateInvoicingDetails(devisId, startMonth);
        return ResponseEntity.ok(generated);
    }

}
