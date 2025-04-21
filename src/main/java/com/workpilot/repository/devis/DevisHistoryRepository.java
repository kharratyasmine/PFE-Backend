package com.workpilot.repository.devis;

import com.workpilot.entity.devis.DevisHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DevisHistoryRepository extends JpaRepository<DevisHistory, Long> {
    List<DevisHistory> findByDevisId(Long devisId);
}
