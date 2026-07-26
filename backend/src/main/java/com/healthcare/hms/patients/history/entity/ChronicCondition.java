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
 * Structured chronic condition entry (diabetes, hypertension, etc.).
 *
 * <p>Distinct from {@link PastDisease}: chronic conditions remain clinically
 * relevant for ongoing care even when {@code CONTROLLED}.
 */
@Entity
@Table(
        name = "chronic_conditions",
        indexes = {
                @Index(name = "idx_chronic_conditions_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_chronic_conditions_patient_id", columnList = "tenant_id, patient_id"),
                @Index(name = "idx_chronic_conditions_medical_history_id", columnList = "medical_history_id"),
                @Index(name = "idx_chronic_conditions_diagnosis_date", columnList = "tenant_id, patient_id, diagnosis_date"),
                @Index(name = "idx_chronic_conditions_status", columnList = "tenant_id, patient_id, condition_status"),
                @Index(name = "idx_chronic_conditions_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class ChronicCondition extends ClinicalHistoryEntry {

    @NotBlank
    @Size(max = 200)
    @Column(name = "condition_name", nullable = false, length = 200)
    private String conditionName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "disease_category", nullable = false, length = 30)
    private DiseaseCategory diseaseCategory = DiseaseCategory.OTHER;

    /** Optional coded identifier (e.g. ICD-10). */
    @Size(max = 32)
    @Column(name = "condition_code", length = 32)
    private String conditionCode;

    public String getConditionName() {
        return conditionName;
    }

    public void setConditionName(final String conditionName) {
        this.conditionName = conditionName;
    }

    public DiseaseCategory getDiseaseCategory() {
        return diseaseCategory;
    }

    public void setDiseaseCategory(final DiseaseCategory diseaseCategory) {
        this.diseaseCategory = diseaseCategory;
    }

    public String getConditionCode() {
        return conditionCode;
    }

    public void setConditionCode(final String conditionCode) {
        this.conditionCode = conditionCode;
    }
}
