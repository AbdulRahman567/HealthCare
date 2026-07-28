package com.healthcare.hms.patients.history.entity;

import com.healthcare.hms.common.persistence.TenantOwnedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

/**
 * Patient medical-history chart root (1:1 with {@code patients}).
 *
 * <p>Holds no free-text summary blob — clinical facts live in structured child
 * entries ({@link PastDisease}, {@link SurgeryHistory}, {@link ChronicCondition},
 * {@link FamilyHistory}).
 */
@Entity
@Table(
        name = "medical_histories",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_medical_histories_tenant_patient", columnNames = {"tenant_id", "patient_id"})
        },
        indexes = {
                @Index(name = "idx_medical_histories_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_medical_histories_patient_id", columnList = "patient_id"),
                @Index(name = "idx_medical_histories_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class MedicalHistory extends TenantOwnedEntity {

    @NotNull
    @Column(name = "patient_id", nullable = false, updatable = false, columnDefinition = "CHAR(36)")
    private UUID patientId;

    @Column(name = "last_reviewed_at")
    private Instant lastReviewedAt;

    @Column(name = "last_reviewed_by", columnDefinition = "CHAR(36)")
    private UUID lastReviewedBy;

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(final UUID patientId) {
        this.patientId = patientId;
    }

    public Instant getLastReviewedAt() {
        return lastReviewedAt;
    }

    public void setLastReviewedAt(final Instant lastReviewedAt) {
        this.lastReviewedAt = lastReviewedAt;
    }

    public UUID getLastReviewedBy() {
        return lastReviewedBy;
    }

    public void setLastReviewedBy(final UUID lastReviewedBy) {
        this.lastReviewedBy = lastReviewedBy;
    }

    public void markReviewed(final UUID reviewerUserId) {
        this.lastReviewedAt = Instant.now();
        this.lastReviewedBy = reviewerUserId;
    }
}
