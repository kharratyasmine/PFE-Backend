package com.workpilot.repository;

import com.workpilot.auditing.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUsernameContainingIgnoreCase(String username);
    List<AuditLog> findByEntityAffectedContainingIgnoreCase(String entity);

}


