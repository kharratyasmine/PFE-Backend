package com.workpilot.repository.Psr;

import com.workpilot.entity.PSR.Psr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PsrRepository extends JpaRepository<Psr, Long> {

    List<Psr> findByProjectId(Long projectId);

}