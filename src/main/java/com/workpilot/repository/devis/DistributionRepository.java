package com.workpilot.repository.devis;

import com.workpilot.entity.devis.Distribution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DistributionRepository extends JpaRepository<Distribution, Long> {
    List<Distribution> findByDevisId(Long devisId);
}
