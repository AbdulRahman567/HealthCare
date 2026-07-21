package com.healthcare.hms.audit.service;

import com.healthcare.hms.audit.enums.AuditAction;
import java.util.UUID;

/**
 * Records critical system and security events for compliance tracing.
 */
public interface AuditLogService {

    void record(
            UUID tenantId,
            UUID userId,
            String entityType,
            String entityId,
            AuditAction action,
            String oldValue,
            String newValue,
            String ipAddress,
            String userAgent
    );
}
