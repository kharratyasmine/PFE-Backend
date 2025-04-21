package com.workpilot.repository.devis;

import com.workpilot.entity.devis.FinancialDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FinancialDetailRepository extends JpaRepository<FinancialDetail, Long> {
    List<FinancialDetail> findByDevisId(Long devisId);
    void deleteByDevis_Id(Long devisId);


}

