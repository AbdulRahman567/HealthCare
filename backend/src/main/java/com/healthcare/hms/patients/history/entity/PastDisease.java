package com.healthcare.hms.patients.history.entity;

import com.healthcare.hms.patients.history.enums.DiseaseCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.SQLRestriction;

/**
 * Structured past (typically resolved or historical) disease entry.
 */
@Entity
@Table(
        name = "past_diseases",
        indexes = {
                @Index(name = "idx_past_diseases_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_past_diseases_patient_id", columnList = "tenant_id, patient_id"),
                @Index(name = "idx_past_diseases_medical_history_id", columnList = "medical_history_id"),
                @Index(name = "idx_past_diseases_diagnosis_date", columnList = "tenant_id, patient_id, diagnosis_date"),
                @Index(name = "idx_past_diseases_status", columnList = "tenant_id, patient_id, condition_status"),
                @Index(name = "idx_past_diseases_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class PastDisease extends ClinicalHistoryEntry {

    @NotBlank
    @Size(max = 200)
    @Column(name = "disease_name", nullable = false, length = 200)
    private String diseaseName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "disease_category", nullable = false, length = 30)
    private DiseaseCategory diseaseCategory = DiseaseCategory.OTHER;

    /** Optional coded identifier (e.g. ICD-10), not free-form narrative. */
    @Size(max = 32)
    @Column(name = "disease_code", length = 32)
    private String diseaseCode;

    public String getDiseaseName() {
        return diseaseName;
    }

    public void setDiseaseName(final String diseaseName) {
        this.diseaseName = diseaseName;
    }

    public DiseaseCategory getDiseaseCategory() {
        return diseaseCategory;
    }

    public void setDiseaseCategory(final DiseaseCategory diseaseCategory) {
        this.diseaseCategory = diseaseCategory;
    }

    public String getDiseaseCode() {
        return diseaseCode;
    }

    public void setDiseaseCode(final String diseaseCode) {
        this.diseaseCode = diseaseCode;
    }
}
