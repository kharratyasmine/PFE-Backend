package com.workpilot.service;

import com.workpilot.entity.InvoicingDetail;

import java.util.List;

public interface InvoicingDetailService {
    InvoicingDetail createInvoicingDetail(InvoicingDetail invoicingDetail);
    InvoicingDetail updateInvoicingDetail(Long id, InvoicingDetail updatedDetail);

    List<InvoicingDetail> GetAllInvoicingDetail();

    InvoicingDetail getInvoicingDetailById(Long id);

    void deleteInvoicingDetail(Long id);
}
