package com.workpilot.service;

import com.workpilot.entity.FinancialDetail;

import java.util.List;

public interface FinancialDetailService {
    List<FinancialDetail> GetAllFinancialDetail();

    FinancialDetail createFinancialDetail(FinancialDetail financialDetail);

    FinancialDetail updateFinancialDetail(Long id, FinancialDetail updatedDetail);

    FinancialDetail getFinancialDetailById(Long id);

    void deleteFinancialDetail(Long id);
}
