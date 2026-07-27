-- Phase 6.6: Appointment search — composite indexes for filtered / sorted lists.
-- Name and queue filters use EXISTS subqueries; these indexes keep those lookups indexable.

CREATE INDEX idx_appointments_tenant_visit_date
    ON appointments (tenant_id, visit_type, appointment_date);

CREATE INDEX idx_appointments_tenant_status_visit_date
    ON appointments (tenant_id, status, visit_type, appointment_date);

CREATE INDEX idx_queue_entries_tenant_status_appointment
    ON queue_entries (tenant_id, status, appointment_id);

CREATE INDEX idx_users_tenant_last_first
    ON users (tenant_id, last_name, first_name);
