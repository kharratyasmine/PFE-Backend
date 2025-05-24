package com.workpilot.repository.devis;

import com.workpilot.entity.devis.Devis;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface DevisRepository extends JpaRepository<Devis, Long> {
    List<Devis> findByProjectId(Long projectId);
    @Modifying
    @Transactional
    @Query("DELETE FROM Devis d WHERE d.demande.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT DISTINCT d FROM Devis d LEFT JOIN FETCH d.financialDetails WHERE d.project.id = :projectId")
    List<Devis> findByProjectIdWithDetails(@Param("projectId") Long projectId);


}
