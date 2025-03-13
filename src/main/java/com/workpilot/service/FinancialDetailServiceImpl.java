package com.workpilot.service;

import com.workpilot.entity.FinancialDetail;
import com.workpilot.repository.FinancialDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinancialDetailServiceImpl implements FinancialDetailService {

    @Autowired
    private FinancialDetailRepository financialDetailRepository;

    @Override
    public List<FinancialDetail> GetAllFinancialDetail() {
        return financialDetailRepository.findAll();
    }
    @Override
    public FinancialDetail createFinancialDetail(FinancialDetail financialDetail) {
        return financialDetailRepository.save(financialDetail);
    }

    @Override
    public FinancialDetail updateFinancialDetail(Long id, FinancialDetail updatedDetail) {
        FinancialDetail existingDetail = financialDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FinancialDetail avec ID " + id + " introuvable."));

        if (updatedDetail.getPosition() != null) existingDetail.setPosition(updatedDetail.getPosition());
        if (updatedDetail.getWorkload() != null) existingDetail.setWorkload(updatedDetail.getWorkload());
        if (updatedDetail.getDailyCost() != null) existingDetail.setDailyCost(updatedDetail.getDailyCost());
        if (updatedDetail.getTotalCost() != null) existingDetail.setTotalCost(updatedDetail.getTotalCost());

        return financialDetailRepository.save(existingDetail);
    }

    @Override
    public FinancialDetail getFinancialDetailById(Long id) {
        return financialDetailRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FinancialDetail avec ID " + id + " introuvable."));
    }

    @Override
    public void deleteFinancialDetail(Long id) {
        financialDetailRepository.deleteById(id);
    }
}
