package com.workpilot.controller.DevisController;

import com.workpilot.entity.devis.FinancialDetail;
import com.workpilot.service.DevisServices.FinancialDetail.FinancialDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/financialDetails")
@CrossOrigin(origins = "http://localhost:4200")
public class FinancialDetailController {

    @Autowired
    private FinancialDetailService financialDetailService;


    @GetMapping
    public ResponseEntity<List<FinancialDetail>> GetAllFinancialDetail() {
        List<FinancialDetail> financialDetail = financialDetailService.GetAllFinancialDetail();
        return ResponseEntity.ok(financialDetail);

    }
    @PostMapping
    public ResponseEntity<FinancialDetail> createFinancialDetail(@RequestBody FinancialDetail financialDetail) {
        return ResponseEntity.ok(financialDetailService.createFinancialDetail(financialDetail));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FinancialDetail> updateFinancialDetail(@PathVariable Long id, @RequestBody FinancialDetail financialDetail) {
        return ResponseEntity.ok(financialDetailService.updateFinancialDetail(id, financialDetail));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FinancialDetail> getFinancialDetailById(@PathVariable Long id) {
        return ResponseEntity.ok(financialDetailService.getFinancialDetailById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFinancialDetail(@PathVariable Long id) {
        financialDetailService.deleteFinancialDetail(id);
        return ResponseEntity.noContent().build();
    }
    // Récupérer les FinancialDetails associés à un devis
    @GetMapping("/devis/{devisId}")
    public ResponseEntity<List<FinancialDetail>> getByDevis(@PathVariable Long devisId) {
        List<FinancialDetail> details = financialDetailService.getByDevisId(devisId);
        return ResponseEntity.ok(details);
    }
}
