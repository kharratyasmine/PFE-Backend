package com.workpilot.service.DevisServices.InvoicingDetail;

import com.workpilot.dto.DevisDTO.InvoicingDetailDTO;
import com.workpilot.entity.devis.InvoicingDetail;

import java.util.List;

public interface InvoicingDetailService {
    InvoicingDetail createInvoicingDetail(InvoicingDetail invoicingDetail);
    InvoicingDetail updateInvoicingDetail(Long id, InvoicingDetail updatedDetail);

    List<InvoicingDetail> GetAllInvoicingDetail();

    InvoicingDetail getInvoicingDetailById(Long id);

    void deleteInvoicingDetail(Long id);

    List<InvoicingDetail> getByDevisId(Long devisId);
    List<InvoicingDetailDTO> generateInvoicingDetails(Long devisId, int startMonth);

}
