package com.workpilot.repository.devis;

import com.workpilot.entity.devis.Visa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface VisaRepository extends JpaRepository<Visa, Long> {
    List<Visa> findByDevisId(Long devisId);
    boolean existsByActionAndDevisId(String action, Long devisId);

}
