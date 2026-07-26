package com.healthcare.hms.patients.entity;

import com.healthcare.hms.common.persistence.TenantOwnedEntity;
import com.healthcare.hms.patients.enums.BloodGroup;
import com.healthcare.hms.patients.enums.Gender;
import com.healthcare.hms.patients.enums.MaritalStatus;
import com.healthcare.hms.patients.enums.PatientStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.SQLRestriction;

/**
 * Tenant-owned patient registration (demographics + identifiers).
 *
 * <p>Cross-cutting guarantees (inherited from {@link TenantOwnedEntity} /
 * {@link com.healthcare.hms.common.persistence.BaseEntity}): UUID primary key,
 * {@code tenant_id}, audit columns, soft delete, optimistic lock ({@code version}).
 *
 * <p>MRN is unique per tenant among non-deleted rows (see Flyway
 * {@code active_mrn_slot} generated column). Soft-deleted rows free the MRN
 * for reuse when a mistaken registration is corrected. National ID, when
 * present, is similarly unique among live rows via {@code active_national_id_slot}.
 *
 * <p>Lifecycle: {@link #deactivate()} / {@link #reactivate()} — no physical delete
 * via Phase 5.2 APIs.
 */
@Entity
@Table(
        name = "patients",
        indexes = {
                @Index(name = "idx_patients_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_patients_tenant_mrn", columnList = "tenant_id, mrn"),
                @Index(name = "idx_patients_tenant_status", columnList = "tenant_id, status"),
                @Index(name = "idx_patients_tenant_last_first", columnList = "tenant_id, last_name, first_name"),
                @Index(name = "idx_patients_tenant_phone", columnList = "tenant_id, phone"),
                @Index(name = "idx_patients_tenant_email", columnList = "tenant_id, email"),
                @Index(name = "idx_patients_tenant_dob", columnList = "tenant_id, date_of_birth"),
                @Index(name = "idx_patients_tenant_gender", columnList = "tenant_id, gender"),
                @Index(name = "idx_patients_tenant_blood_group", columnList = "tenant_id, blood_group"),
                @Index(name = "idx_patients_tenant_status_name", columnList = "tenant_id, status, last_name, first_name"),
                @Index(name = "idx_patients_tenant_department", columnList = "tenant_id, primary_department_id"),
                @Index(name = "idx_patients_tenant_doctor", columnList = "tenant_id, primary_doctor_id"),
                @Index(name = "idx_patients_tenant_created_at", columnList = "tenant_id, created_at"),
                @Index(name = "idx_patients_deleted", columnList = "deleted")
        }
)
@SQLRestriction("deleted = false")
public class Patient extends TenantOwnedEntity {

    /**
     * Medical Record Number — hospital-facing patient identifier.
     * Unique per tenant for live (non-deleted) rows.
     */
    @NotBlank
    @Size(max = 50)
    @Column(name = "mrn", nullable = false, length = 50)
    private String mrn;

    @NotBlank
    @Size(max = 100)
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotNull
    @PastOrPresent
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 20)
    private Gender gender = Gender.UNKNOWN;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false, length = 20)
    private BloodGroup bloodGroup = BloodGroup.UNKNOWN;

    /**
     * National identity document number (CNIC or passport). Optional at registration.
     */
    @Size(max = 50)
    @Column(name = "national_id", length = 50)
    private String nationalId;

    @Size(max = 30)
    @Column(name = "phone", length = 30)
    private String phone;

    @Email
    @Size(max = 255)
    @Column(name = "email", length = 255)
    private String email;

    @Size(max = 500)
    @Column(name = "address", length = 500)
    private String address;

    @Valid
    @Embedded
    private EmergencyContact emergencyContact = new EmergencyContact();

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", length = 20)
    private MaritalStatus maritalStatus;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PatientStatus status = PatientStatus.ACTIVE;

    /**
     * Optional primary / last-known department affiliation for directory search.
     * Populated by later assignment workflows; nullable until then.
     */
    @Column(name = "primary_department_id", columnDefinition = "CHAR(36)")
    private UUID primaryDepartmentId;

    /**
     * Optional primary / PCP doctor for directory search (future-ready).
     * References {@code doctors.id}; nullable until assignment workflows exist.
     */
    @Column(name = "primary_doctor_id", columnDefinition = "CHAR(36)")
    private UUID primaryDoctorId;

    public String getMrn() {
        return mrn;
    }

    public void setMrn(final String mrn) {
        this.mrn = mrn;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(final LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(final Gender gender) {
        this.gender = gender;
    }

    public BloodGroup getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(final BloodGroup bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(final String nationalId) {
        this.nationalId = nationalId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(final String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address;
    }

    public EmergencyContact getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(final EmergencyContact emergencyContact) {
        this.emergencyContact = emergencyContact != null ? emergencyContact : new EmergencyContact();
    }

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(final MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public PatientStatus getStatus() {
        return status;
    }

    public void setStatus(final PatientStatus status) {
        this.status = status;
    }

    public UUID getPrimaryDepartmentId() {
        return primaryDepartmentId;
    }

    public void setPrimaryDepartmentId(final UUID primaryDepartmentId) {
        this.primaryDepartmentId = primaryDepartmentId;
    }

    public UUID getPrimaryDoctorId() {
        return primaryDoctorId;
    }

    public void setPrimaryDoctorId(final UUID primaryDoctorId) {
        this.primaryDoctorId = primaryDoctorId;
    }

    /**
     * Deactivate registration: {@code ACTIVE} → {@code INACTIVE}.
     * Chart is retained; no physical deletion.
     */
    public void deactivate() {
        if (status != PatientStatus.ACTIVE) {
            throw new IllegalStateException("Cannot deactivate patient from status " + status);
        }
        this.status = PatientStatus.INACTIVE;
    }

    /**
     * Reactivate registration: {@code INACTIVE} → {@code ACTIVE}.
     */
    public void reactivate() {
        if (status != PatientStatus.INACTIVE) {
            throw new IllegalStateException("Cannot reactivate patient from status " + status);
        }
        this.status = PatientStatus.ACTIVE;
    }
}
