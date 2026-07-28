package com.healthcare.hms.clinical.entity;

import com.healthcare.hms.common.persistence.TenantOwnedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

/**
 * Vital signs captured during a {@link Consultation}.
 *
 * <p>Multiple readings per consultation are supported (e.g. nurse pre-check and
 * doctor re-check). Values are stored as typed numerics — not free-text strings —
 * to enable trend analysis across encounters.
 */
@Entity
@Table(
        name = "vital_signs",
        indexes = {
                @Index(name = "idx_vital_signs_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_vital_signs_consultation", columnList = "tenant_id, consultation_id"),
                @Index(name = "idx_vital_signs_patient", columnList = "tenant_id, patient_id"),
                @Index(name = "idx_vital_signs_recorded_at", columnList = "tenant_id, patient_id, recorded_at"),
                @Index(name = "idx_vital_signs_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class VitalSigns extends TenantOwnedEntity {

    @NotNull
    @Column(name = "consultation_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID consultationId;

    @NotNull
    @Column(name = "patient_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID patientId;

    /**
     * Staff user who recorded the vitals ({@code users.id}) — may be nurse or doctor.
     */
    @Column(name = "recorded_by_user_id", columnDefinition = "CHAR(36)")
    private UUID recordedByUserId;

    @NotNull
    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "temperature_celsius", precision = 4, scale = 1)
    private BigDecimal temperatureCelsius;

    @Min(40)
    @Max(300)
    @Column(name = "systolic_bp")
    private Integer systolicBp;

    @Min(20)
    @Max(200)
    @Column(name = "diastolic_bp")
    private Integer diastolicBp;

    @Min(20)
    @Max(300)
    @Column(name = "pulse_bpm")
    private Integer pulseBpm; // heart rate (bpm)

    @Min(4)
    @Max(80)
    @Column(name = "respiratory_rate")
    private Integer respiratoryRate;

    @Column(name = "oxygen_saturation_percent", precision = 4, scale = 1)
    private BigDecimal oxygenSaturationPercent;

    @Column(name = "blood_glucose_mg_dl", precision = 6, scale = 2)
    private BigDecimal bloodGlucoseMgDl;

    /** Snapshot BMI at time of recording (derived from height/weight when available). */
    @Column(name = "bmi", precision = 4, scale = 1)
    private BigDecimal bmi;

    @Min(0)
    @Max(10)
    @Column(name = "pain_scale")
    private Integer painScale;

    @Size(max = 500)
    @Column(name = "notes", length = 500)
    private String notes;

    public UUID getConsultationId() {
        return consultationId;
    }

    public void setConsultationId(final UUID consultationId) {
        this.consultationId = consultationId;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(final UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getRecordedByUserId() {
        return recordedByUserId;
    }

    public void setRecordedByUserId(final UUID recordedByUserId) {
        this.recordedByUserId = recordedByUserId;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(final Instant recordedAt) {
        this.recordedAt = recordedAt;
    }

    public BigDecimal getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(final BigDecimal heightCm) {
        this.heightCm = heightCm;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(final BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public BigDecimal getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public void setTemperatureCelsius(final BigDecimal temperatureCelsius) {
        this.temperatureCelsius = temperatureCelsius;
    }

    public Integer getSystolicBp() {
        return systolicBp;
    }

    public void setSystolicBp(final Integer systolicBp) {
        this.systolicBp = systolicBp;
    }

    public Integer getDiastolicBp() {
        return diastolicBp;
    }

    public void setDiastolicBp(final Integer diastolicBp) {
        this.diastolicBp = diastolicBp;
    }

    public Integer getPulseBpm() {
        return pulseBpm;
    }

    public void setPulseBpm(final Integer pulseBpm) {
        this.pulseBpm = pulseBpm;
    }

    public Integer getRespiratoryRate() {
        return respiratoryRate;
    }

    public void setRespiratoryRate(final Integer respiratoryRate) {
        this.respiratoryRate = respiratoryRate;
    }

    public BigDecimal getOxygenSaturationPercent() {
        return oxygenSaturationPercent;
    }

    public void setOxygenSaturationPercent(final BigDecimal oxygenSaturationPercent) {
        this.oxygenSaturationPercent = oxygenSaturationPercent;
    }

    public BigDecimal getBloodGlucoseMgDl() {
        return bloodGlucoseMgDl;
    }

    public void setBloodGlucoseMgDl(final BigDecimal bloodGlucoseMgDl) {
        this.bloodGlucoseMgDl = bloodGlucoseMgDl;
    }

    public BigDecimal getBmi() {
        return bmi;
    }

    public void setBmi(final BigDecimal bmi) {
        this.bmi = bmi;
    }

    public Integer getPainScale() {
        return painScale;
    }

    public void setPainScale(final Integer painScale) {
        this.painScale = painScale;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }
}
