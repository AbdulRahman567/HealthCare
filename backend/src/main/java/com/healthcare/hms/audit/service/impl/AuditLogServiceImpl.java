package com.healthcare.hms.audit.service.impl;

import com.healthcare.hms.audit.entity.AuditLog;
import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.repository.AuditLogRepository;
import com.healthcare.hms.audit.service.AuditLogService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(final AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(
            final UUID tenantId,
            final UUID userId,
            final String entityType,
            final String entityId,
            final AuditAction action,
            final String oldValue,
            final String newValue,
            final String ipAddress,
            final String userAgent
    ) {
        final AuditLog auditLog = new AuditLog();
        auditLog.setTenantId(tenantId);
        auditLog.setUserId(userId);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setAction(action);
        auditLog.setOldValue(oldValue);
        auditLog.setNewValue(newValue);
        auditLog.setIpAddress(truncate(ipAddress, 45));
        auditLog.setUserAgent(truncate(userAgent, 512));
        auditLogRepository.save(auditLog);
    }

    private static String truncate(final String value, final int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
