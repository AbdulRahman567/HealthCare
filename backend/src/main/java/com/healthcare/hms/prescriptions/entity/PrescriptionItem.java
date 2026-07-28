package com.healthcare.hms.prescriptions.entity;

import com.healthcare.hms.common.persistence.TenantOwnedEntity;
import com.healthcare.hms.prescriptions.enums.MedicationRoute;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

/**
 * Single medicine line on a {@link Prescription}.
 *
 * <p>{@code medicine_name_key} enforces case-insensitive uniqueness per prescription
 * among live rows. {@code medicine_id} / {@code medicine_code} are reserved for
 * future medicine-master and pharmacy catalog integration.
 */
@Entity
@Table(
        name = "prescription_items",
        indexes = {
                @Index(name = "idx_prescription_items_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_prescription_items_prescription", columnList = "tenant_id, prescription_id"),
                @Index(name = "idx_prescription_items_patient", columnList = "tenant_id, patient_id"),
                @Index(name = "idx_prescription_items_medicine_id", columnList = "tenant_id, medicine_id"),
                @Index(name = "idx_prescription_items_medicine_code", columnList = "tenant_id, medicine_code"),
                @Index(name = "idx_prescription_items_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class PrescriptionItem extends TenantOwnedEntity {

    @NotNull
    @Column(name = "prescription_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID prescriptionId;

    @NotNull
    @Column(name = "patient_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID patientId;

    @NotBlank
    @Size(max = 200)
    @Column(name = "medicine_name", nullable = false, length = 200)
    private String medicineName;

    /**
     * Normalized medicine name for duplicate detection (lowercase trimmed).
     */
    @NotBlank
    @Size(max = 200)
    @Column(name = "medicine_name_key", nullable = false, length = 200)
    private String medicineNameKey;

    /** Optional FK to future medicine_master catalog. */
    @Column(name = "medicine_id", columnDefinition = "CHAR(36)")
    private UUID medicineId;

    /** Optional external / formulary code for pharmacy systems. */
    @Size(max = 64)
    @Column(name = "medicine_code", length = 64)
    private String medicineCode;

    @NotBlank
    @Size(max = 100)
    @Column(name = "dosage", nullable = false, length = 100)
    private String dosage;

    @NotBlank
    @Size(max = 100)
    @Column(name = "frequency", nullable = false, length = 100)
    private String frequency;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "route", nullable = false, length = 30)
    private MedicationRoute route = MedicationRoute.ORAL;

    @NotBlank
    @Size(max = 100)
    @Column(name = "duration", nullable = false, length = 100)
    private String duration;

    @Size(max = 1000)
    @Column(name = "instructions", length = 1000)
    private String instructions;

    @NotNull
    @Min(1)
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @NotNull
    @Min(0)
    @Column(name = "refills", nullable = false)
    private Integer refills = 0;

    @NotNull
    @Min(1)
    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber = 1;

    @NotNull
    @Column(name = "before_food", nullable = false)
    private Boolean beforeFood = false;

    @NotNull
    @Column(name = "after_food", nullable = false)
    private Boolean afterFood = false;

    public UUID getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(final UUID prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(final UUID patientId) {
        this.patientId = patientId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(final String medicineName) {
        this.medicineName = medicineName;
    }

    public String getMedicineNameKey() {
        return medicineNameKey;
    }

    public void setMedicineNameKey(final String medicineNameKey) {
        this.medicineNameKey = medicineNameKey;
    }

    public UUID getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(final UUID medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineCode() {
        return medicineCode;
    }

    public void setMedicineCode(final String medicineCode) {
        this.medicineCode = medicineCode;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(final String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(final String frequency) {
        this.frequency = frequency;
    }

    public MedicationRoute getRoute() {
        return route;
    }

    public void setRoute(final MedicationRoute route) {
        this.route = route;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(final String duration) {
        this.duration = duration;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(final String instructions) {
        this.instructions = instructions;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getRefills() {
        return refills;
    }

    public void setRefills(final Integer refills) {
        this.refills = refills;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(final Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Boolean getBeforeFood() {
        return beforeFood;
    }

    public void setBeforeFood(final Boolean beforeFood) {
        this.beforeFood = beforeFood;
    }

    public Boolean getAfterFood() {
        return afterFood;
    }

    public void setAfterFood(final Boolean afterFood) {
        this.afterFood = afterFood;
    }
}
