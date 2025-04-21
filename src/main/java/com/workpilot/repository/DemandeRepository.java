package com.workpilot.repository;

import com.workpilot.entity.ressources.Demande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandeRepository extends JpaRepository<Demande, Long> {
       List<Demande> findByProjectId(Long projectId);
}
