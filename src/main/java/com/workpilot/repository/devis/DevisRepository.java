package com.workpilot.repository.devis;

import com.workpilot.entity.devis.Devis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DevisRepository extends JpaRepository<Devis, Long> {
    List<Devis> findByProjectId(Long projectId);

}
