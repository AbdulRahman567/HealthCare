-- Add trial_ends_at column to tenants for free trial tracking
ALTER TABLE tenants
    ADD COLUMN trial_ends_at TIMESTAMP(6) NULL AFTER subscription_plan;

CREATE INDEX idx_tenants_trial_ends_at ON tenants (trial_ends_at);
