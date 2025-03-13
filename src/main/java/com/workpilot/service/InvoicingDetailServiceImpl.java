package com.workpilot.service;

import com.workpilot.entity.InvoicingDetail;
import com.workpilot.repository.InvoicingDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvoicingDetailServiceImpl implements InvoicingDetailService {

    @Autowired
    private InvoicingDetailRepository invoicingDetailRepository;

    @Override
    public List<InvoicingDetail> GetAllInvoicingDetail() {
        return invoicingDetailRepository.findAll();
    }
    @Override
    public InvoicingDetail getInvoicingDetailById(Long id) {
        return invoicingDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("InvoicingDetail avec ID " + id + " introuvable."));
    }
    @Override
    public InvoicingDetail createInvoicingDetail(InvoicingDetail invoicingDetail) {
        return invoicingDetailRepository.save(invoicingDetail);
    }

    @Override
    public InvoicingDetail updateInvoicingDetail(Long id, InvoicingDetail updatedDetail) {
        InvoicingDetail existingDetail = invoicingDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("InvoicingDetail avec ID " + id + " introuvable."));

        if (updatedDetail.getDescription() != null) existingDetail.setDescription(updatedDetail.getDescription());
        if (updatedDetail.getInvoicingDate() != null) existingDetail.setInvoicingDate(updatedDetail.getInvoicingDate());
        if (updatedDetail.getAmount() != null) existingDetail.setAmount(updatedDetail.getAmount());
        if (updatedDetail.getStatus() != null) existingDetail.setStatus(updatedDetail.getStatus());

        return invoicingDetailRepository.save(existingDetail);
    }

    @Override
    public void deleteInvoicingDetail(Long id) {
        invoicingDetailRepository.deleteById(id);
    }
}
