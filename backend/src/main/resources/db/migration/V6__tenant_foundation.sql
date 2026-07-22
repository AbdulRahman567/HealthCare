-- Phase 2.1: Multi-tenant foundation enhancements.
-- Adds tenant_type discriminator, composite indexes, and referential integrity
-- from tenant-scoped tables to the tenants aggregate root.

ALTER TABLE tenants
    ADD COLUMN tenant_type VARCHAR(50) NOT NULL DEFAULT 'HOSPITAL' AFTER name;

CREATE INDEX idx_tenants_tenant_type ON tenants (tenant_type);
CREATE INDEX idx_tenants_status_deleted ON tenants (status, deleted);

-- Referential integrity: tenant_id on identity/audit tables points at tenants.
-- NULL tenant_id remains allowed for platform-scoped rows (Super Admin, system roles).

ALTER TABLE users
    ADD CONSTRAINT fk_users_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id);

ALTER TABLE roles
    ADD CONSTRAINT fk_roles_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id);

ALTER TABLE refresh_tokens
    ADD CONSTRAINT fk_refresh_tokens_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id);

ALTER TABLE audit_logs
    ADD CONSTRAINT fk_audit_logs_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id);

ALTER TABLE password_reset_tokens
    ADD CONSTRAINT fk_password_reset_tokens_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id);

ALTER TABLE email_verification_tokens
    ADD CONSTRAINT fk_email_verification_tokens_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id);
