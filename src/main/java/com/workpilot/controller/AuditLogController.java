package com.workpilot.controller;

import com.workpilot.auditing.AuditLog;
import com.workpilot.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Sort;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(params = "user")
    public List<AuditLog> getLogsByUser(@RequestParam String user) {
        return auditLogRepository.findByUsernameContainingIgnoreCase(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(params = "project")
    public List<AuditLog> getLogsByProject(@RequestParam String project) {
        return auditLogRepository.findByEntityAffectedContainingIgnoreCase(project);
    }
}

