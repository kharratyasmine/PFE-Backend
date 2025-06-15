package com.workpilot.repository.Psr;

import com.workpilot.entity.PSR.PsrGenerationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PsrGenerationLogRepository extends JpaRepository<PsrGenerationLog, Long> {
}
