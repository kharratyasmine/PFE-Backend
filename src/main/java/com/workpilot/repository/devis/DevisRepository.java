package com.workpilot.repository.devis;

import com.workpilot.entity.devis.Devis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface DevisRepository extends JpaRepository<Devis, Long> {
    List<Devis> findByProjectId(Long projectId);

}
