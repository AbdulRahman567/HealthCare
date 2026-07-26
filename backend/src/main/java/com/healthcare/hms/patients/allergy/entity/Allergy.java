package com.healthcare.hms.patients.allergy.entity;

import com.healthcare.hms.common.persistence.TenantOwnedEntity;
import com.healthcare.hms.patients.allergy.enums.AllergyStatus;
import com.healthcare.hms.patients.allergy.enums.AllergyType;
import com.healthcare.hms.patients.allergy.enums.Reaction;
import com.healthcare.hms.patients.allergy.enums.Severity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

/**
 * Safety-critical patient allergy record.
 *
 * <p>Allergies are not metadata — they must surface via banner and critical-alert
 * APIs before prescribing or clinical decision-making (healthcare-domain rule).
 *
 * <p>Structured fields only: type, severity, reaction enums; bounded notes;
 * clinical flags for verified / critical / banner visibility.
 */
@Entity
@Table(
        name = "patient_allergies",
        indexes = {
                @Index(name = "idx_patient_allergies_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_patient_allergies_patient_id", columnList = "tenant_id, patient_id"),
                @Index(name = "idx_patient_allergies_type", columnList = "tenant_id, patient_id, allergy_type"),
                @Index(name = "idx_patient_allergies_severity", columnList = "tenant_id, patient_id, severity"),
                @Index(name = "idx_patient_allergies_status", columnList = "tenant_id, patient_id, status"),
                @Index(name = "idx_patient_allergies_critical", columnList = "tenant_id, patient_id, critical_alert"),
                @Index(name = "idx_patient_allergies_banner", columnList = "tenant_id, patient_id, show_on_banner"),
                @Index(name = "idx_patient_allergies_onset_date", columnList = "tenant_id, patient_id, onset_date"),
                @Index(name = "idx_patient_allergies_created_at", columnList = "tenant_id, patient_id, created_at"),
                @Index(name = "idx_patient_allergies_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class Allergy extends TenantOwnedEntity {

    @NotNull
    @Column(name = "patient_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID patientId;

    @NotBlank
    @Size(max = 200)
    @Column(name = "allergen_name", nullable = false, length = 200)
    private String allergenName;

    /** Optional coded allergen (RxNorm / local drug code / food code). */
    @Size(max = 64)
    @Column(name = "allergen_code", length = 64)
    private String allergenCode;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "allergy_type", nullable = false, length = 30)
    private AllergyType allergyType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 30)
    private Severity severity;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "reaction", nullable = false, length = 30)
    private Reaction reaction;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AllergyStatus status = AllergyStatus.ACTIVE;

    @PastOrPresent
    @Column(name = "onset_date")
    private LocalDate onsetDate;

    @Size(max = 1000)
    @Column(name = "clinical_notes", length = 1000)
    private String clinicalNotes;

    // --- Clinical flags (structured, not free-text) ---

    /** Clinician has verified the allergy (vs patient-reported only). */
    @Column(name = "verified", nullable = false)
    private boolean verified = false;

    /** Patient-reported rather than clinician-observed. */
    @Column(name = "patient_reported", nullable = false)
    private boolean patientReported = true;

    /**
     * Forces inclusion in critical-alert surfaces (anaphylaxis-class risk).
     * Auto-set when {@link Severity#LIFE_THREATENING}.
     */
    @Column(name = "critical_alert", nullable = false)
    private boolean criticalAlert = false;

    /**
     * Include on the patient chart banner — must be true for critical allergies.
     */
    @Column(name = "show_on_banner", nullable = false)
    private boolean showOnBanner = true;

    @Column(name = "recorded_by_user_id", columnDefinition = "CHAR(36)")
    private UUID recordedByUserId;

    /**
     * Applies safety-critical defaults: life-threatening → critical + banner.
     * Critical → always banner-visible.
     */
    public void applyClinicalAlertRules() {
        if (severity == Severity.LIFE_THREATENING || reaction == Reaction.ANAPHYLAXIS) {
            this.criticalAlert = true;
            this.showOnBanner = true;
        } else if (this.criticalAlert) {
            this.showOnBanner = true;
        }
    }

    public boolean isLifeThreatening() {
        return severity == Severity.LIFE_THREATENING || reaction == Reaction.ANAPHYLAXIS;
    }

    public boolean isBannerEligible() {
        return status == AllergyStatus.ACTIVE && showOnBanner && !isDeleted();
    }

    public boolean isCriticalEligible() {
        return status == AllergyStatus.ACTIVE && criticalAlert && !isDeleted();
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(final UUID patientId) {
        this.patientId = patientId;
    }

    public String getAllergenName() {
        return allergenName;
    }

    public void setAllergenName(final String allergenName) {
        this.allergenName = allergenName;
    }

    public String getAllergenCode() {
        return allergenCode;
    }

    public void setAllergenCode(final String allergenCode) {
        this.allergenCode = allergenCode;
    }

    public AllergyType getAllergyType() {
        return allergyType;
    }

    public void setAllergyType(final AllergyType allergyType) {
        this.allergyType = allergyType;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(final Severity severity) {
        this.severity = severity;
    }

    public Reaction getReaction() {
        return reaction;
    }

    public void setReaction(final Reaction reaction) {
        this.reaction = reaction;
    }

    public AllergyStatus getStatus() {
        return status;
    }

    public void setStatus(final AllergyStatus status) {
        this.status = status;
    }

    public LocalDate getOnsetDate() {
        return onsetDate;
    }

    public void setOnsetDate(final LocalDate onsetDate) {
        this.onsetDate = onsetDate;
    }

    public String getClinicalNotes() {
        return clinicalNotes;
    }

    public void setClinicalNotes(final String clinicalNotes) {
        this.clinicalNotes = clinicalNotes;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(final boolean verified) {
        this.verified = verified;
    }

    public boolean isPatientReported() {
        return patientReported;
    }

    public void setPatientReported(final boolean patientReported) {
        this.patientReported = patientReported;
    }

    public boolean isCriticalAlert() {
        return criticalAlert;
    }

    public void setCriticalAlert(final boolean criticalAlert) {
        this.criticalAlert = criticalAlert;
    }

    public boolean isShowOnBanner() {
        return showOnBanner;
    }

    public void setShowOnBanner(final boolean showOnBanner) {
        this.showOnBanner = showOnBanner;
    }

    public UUID getRecordedByUserId() {
        return recordedByUserId;
    }

    public void setRecordedByUserId(final UUID recordedByUserId) {
        this.recordedByUserId = recordedByUserId;
    }
}
