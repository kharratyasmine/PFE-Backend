package com.workpilot.repository.devis;

import com.workpilot.entity.devis.FinancialDetail;
import com.workpilot.entity.devis.InvoicingDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoicingDetailRepository extends JpaRepository<InvoicingDetail, Long> {
    List<InvoicingDetail> findByDevisId(Long devisId);
    void deleteByDevis_Id(Long devisId);


}
