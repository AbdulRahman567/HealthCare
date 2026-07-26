-- Phase 5.6: Patient timeline query indexes (on-read aggregation; no materialised table).
-- Allergy clinical/fallback date indexes for chronological fan-out merges.

CREATE INDEX idx_patient_allergies_onset_date
    ON patient_allergies (tenant_id, patient_id, onset_date);

CREATE INDEX idx_patient_allergies_created_at
    ON patient_allergies (tenant_id, patient_id, created_at);
