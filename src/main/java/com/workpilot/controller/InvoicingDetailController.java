package com.workpilot.controller;

import com.workpilot.entity.FinancialDetail;
import com.workpilot.entity.InvoicingDetail;
import com.workpilot.service.InvoicingDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invoicingDetails")
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
}
