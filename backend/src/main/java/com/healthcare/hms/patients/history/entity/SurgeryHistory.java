package com.healthcare.hms.patients.history.entity;

import com.healthcare.hms.patients.history.enums.ProcedureCategory;
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
 * Structured surgical history entry.
 *
 * <p>{@code diagnosisDate} represents the procedure date.
 * {@code recoveryDate} is optional post-operative recovery date.
 */
@Entity
@Table(
        name = "surgery_histories",
        indexes = {
                @Index(name = "idx_surgery_histories_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_surgery_histories_patient_id", columnList = "tenant_id, patient_id"),
                @Index(name = "idx_surgery_histories_medical_history_id", columnList = "medical_history_id"),
                @Index(name = "idx_surgery_histories_diagnosis_date", columnList = "tenant_id, patient_id, diagnosis_date"),
                @Index(name = "idx_surgery_histories_status", columnList = "tenant_id, patient_id, condition_status"),
                @Index(name = "idx_surgery_histories_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class SurgeryHistory extends ClinicalHistoryEntry {

    @NotBlank
    @Size(max = 200)
    @Column(name = "procedure_name", nullable = false, length = 200)
    private String procedureName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "procedure_category", nullable = false, length = 30)
    private ProcedureCategory procedureCategory = ProcedureCategory.OTHER;

    /** Optional coded identifier (e.g. CPT / local procedure code). */
    @Size(max = 32)
    @Column(name = "procedure_code", length = 32)
    private String procedureCode;

    @Size(max = 200)
    @Column(name = "performing_facility", length = 200)
    private String performingFacility;

    public String getProcedureName() {
        return procedureName;
    }

    public void setProcedureName(final String procedureName) {
        this.procedureName = procedureName;
    }

    public ProcedureCategory getProcedureCategory() {
        return procedureCategory;
    }

    public void setProcedureCategory(final ProcedureCategory procedureCategory) {
        this.procedureCategory = procedureCategory;
    }

    public String getProcedureCode() {
        return procedureCode;
    }

    public void setProcedureCode(final String procedureCode) {
        this.procedureCode = procedureCode;
    }

    public String getPerformingFacility() {
        return performingFacility;
    }

    public void setPerformingFacility(final String performingFacility) {
        this.performingFacility = performingFacility;
    }
}
