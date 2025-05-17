package com.workpilot.repository.devis;

import com.workpilot.entity.devis.Distribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface DistributionRepository extends JpaRepository<Distribution, Long> {
    List<Distribution> findByDevisId(Long devisId);
}
