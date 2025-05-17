package com.workpilot.repository.devis;

import com.workpilot.entity.devis.DevisHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface DevisHistoryRepository extends JpaRepository<DevisHistory, Long> {
    List<DevisHistory> findByDevisId(Long devisId);
}
