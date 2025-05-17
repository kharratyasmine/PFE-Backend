package com.workpilot.repository.devis;

import com.workpilot.entity.devis.FinancialDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface FinancialDetailRepository extends JpaRepository<FinancialDetail, Long> {
    List<FinancialDetail> findByDevisId(Long devisId);
    void deleteByDevis_Id(Long devisId);


}

