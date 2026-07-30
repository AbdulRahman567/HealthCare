-- Drop the FK constraint on audit_logs.tenant_id to prevent lock-wait timeouts
-- when audit log writes using REQUIRES_NEW sub-transactions conflict with the
-- outer transaction's exclusive lock on the tenants row.
--
-- Audit logs are append-only and are never cascade-deleted; referential integrity
-- is maintained at the application layer. This constraint added zero safety and
-- caused a real production-class deadlock pattern during hospital registration.
ALTER TABLE audit_logs DROP FOREIGN KEY fk_audit_logs_tenant;
