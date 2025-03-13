package com.workpilot.controller;

import com.workpilot.entity.FinancialDetail;
import com.workpilot.entity.Project;
import com.workpilot.service.FinancialDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/financialDetails")
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
}
