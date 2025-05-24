package com.workpilot.service;

import com.workpilot.auditing.AuditLog;
import com.workpilot.auditing.NoAuditLog;
import com.workpilot.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
@NoAuditLog
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(String username, String action, String entity, String method, String params) {
        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setAction(action);
        log.setEntityAffected(entity);
        log.setMethodName(method);
        log.setTimestamp(LocalDateTime.now());
        log.setParameters(params);
        auditLogRepository.save(log);
    }
}

