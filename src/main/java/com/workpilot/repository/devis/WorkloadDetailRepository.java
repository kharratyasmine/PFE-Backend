package com.workpilot.repository.devis;

import com.workpilot.entity.devis.FinancialDetail;
import com.workpilot.entity.devis.WorkloadDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface WorkloadDetailRepository extends JpaRepository<WorkloadDetail, Long> {

    List<WorkloadDetail> findByDevisId(Long devisId);
    void deleteAllByDevisId(Long devisId);
    @Modifying
    @Query("DELETE FROM WorkloadDetail wd WHERE wd.demande.id IN :demandeIds")
    void deleteByDemandeIds(@Param("demandeIds") List<Long> demandeIds);

    void deleteByDemandeId(Long id);
    void deleteByDevisId(Long id);
}
