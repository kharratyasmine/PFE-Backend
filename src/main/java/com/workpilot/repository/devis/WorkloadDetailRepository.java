package com.workpilot.repository.devis;

import com.workpilot.entity.devis.FinancialDetail;
import com.workpilot.entity.devis.WorkloadDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface WorkloadDetailRepository extends JpaRepository<WorkloadDetail, Long> {

    List<WorkloadDetail> findByDevisId(Long devisId);
    void deleteAllByDevisId(Long devisId);

}
