package com.healthcare.hms.patients.immunization.entity;

import com.healthcare.hms.common.persistence.TenantOwnedEntity;
import com.healthcare.hms.patients.immunization.enums.ImmunizationStatus;
import com.healthcare.hms.patients.immunization.enums.VaccineRoute;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

/**
 * Patient immunization (vaccination) record.
 *
 * <p>Captures vaccine identity, dose in series, manufacturer/batch for lot
 * recall, administration and next-due dates, and administering provider.
 */
@Entity
@Table(
        name = "patient_immunizations",
        indexes = {
                @Index(name = "idx_patient_immunizations_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_patient_immunizations_patient_id", columnList = "tenant_id, patient_id"),
                @Index(name = "idx_patient_immunizations_vaccine", columnList = "tenant_id, patient_id, vaccine_name"),
                @Index(name = "idx_patient_immunizations_status", columnList = "tenant_id, patient_id, status"),
                @Index(name = "idx_patient_immunizations_admin_date", columnList = "tenant_id, patient_id, administration_date"),
                @Index(name = "idx_patient_immunizations_next_due", columnList = "tenant_id, patient_id, next_due_date"),
                @Index(name = "idx_patient_immunizations_batch", columnList = "tenant_id, batch_number"),
                @Index(name = "idx_patient_immunizations_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class Immunization extends TenantOwnedEntity {

    @NotNull
    @Column(name = "patient_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID patientId;

    @NotBlank
    @Size(max = 200)
    @Column(name = "vaccine_name", nullable = false, length = 200)
    private String vaccineName;

    /** Optional coded vaccine (CVX / local formulary code). */
    @Size(max = 64)
    @Column(name = "vaccine_code", length = 64)
    private String vaccineCode;

    /** Dose number in the series (1 = first dose). */
    @NotNull
    @Min(1)
    @Max(50)
    @Column(name = "dose_number", nullable = false)
    private Integer doseNumber;

    @Size(max = 200)
    @Column(name = "manufacturer", length = 200)
    private String manufacturer;

    @Size(max = 100)
    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    @NotNull
    @Column(name = "administration_date", nullable = false)
    private LocalDate administrationDate;

    @Column(name = "next_due_date")
    private LocalDate nextDueDate;

    @NotBlank
    @Size(max = 200)
    @Column(name = "healthcare_provider", nullable = false, length = 200)
    private String healthcareProvider;

    @Enumerated(EnumType.STRING)
    @Column(name = "route", length = 30)
    private VaccineRoute route = VaccineRoute.UNKNOWN;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ImmunizationStatus status = ImmunizationStatus.ADMINISTERED;

    @Size(max = 1000)
    @Column(name = "clinical_notes", length = 1000)
    private String clinicalNotes;

    @Column(name = "recorded_by_user_id", columnDefinition = "CHAR(36)")
    private UUID recordedByUserId;

    /**
     * Whether the next dose is due on or before {@code asOf} (inclusive).
     */
    public boolean isDueOnOrBefore(final LocalDate asOf) {
        return nextDueDate != null
                && status == ImmunizationStatus.ADMINISTERED
                && !nextDueDate.isAfter(asOf)
                && !isDeleted();
    }

    public boolean isUpcoming(final LocalDate asOf, final LocalDate untilInclusive) {
        return nextDueDate != null
                && status == ImmunizationStatus.ADMINISTERED
                && nextDueDate.isAfter(asOf)
                && !nextDueDate.isAfter(untilInclusive)
                && !isDeleted();
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(final UUID patientId) {
        this.patientId = patientId;
    }

    public String getVaccineName() {
        return vaccineName;
    }

    public void setVaccineName(final String vaccineName) {
        this.vaccineName = vaccineName;
    }

    public String getVaccineCode() {
        return vaccineCode;
    }

    public void setVaccineCode(final String vaccineCode) {
        this.vaccineCode = vaccineCode;
    }

    public Integer getDoseNumber() {
        return doseNumber;
    }

    public void setDoseNumber(final Integer doseNumber) {
        this.doseNumber = doseNumber;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(final String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(final String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public LocalDate getAdministrationDate() {
        return administrationDate;
    }

    public void setAdministrationDate(final LocalDate administrationDate) {
        this.administrationDate = administrationDate;
    }

    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(final LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public String getHealthcareProvider() {
        return healthcareProvider;
    }

    public void setHealthcareProvider(final String healthcareProvider) {
        this.healthcareProvider = healthcareProvider;
    }

    public VaccineRoute getRoute() {
        return route;
    }

    public void setRoute(final VaccineRoute route) {
        this.route = route;
    }

    public ImmunizationStatus getStatus() {
        return status;
    }

    public void setStatus(final ImmunizationStatus status) {
        this.status = status;
    }

    public String getClinicalNotes() {
        return clinicalNotes;
    }

    public void setClinicalNotes(final String clinicalNotes) {
        this.clinicalNotes = clinicalNotes;
    }

    public UUID getRecordedByUserId() {
        return recordedByUserId;
    }

    public void setRecordedByUserId(final UUID recordedByUserId) {
        this.recordedByUserId = recordedByUserId;
    }
}
