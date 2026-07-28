package com.healthcare.hms.clinical.support;

import com.healthcare.hms.clinical.dto.request.ConsultationSearchCriteria;
import com.healthcare.hms.clinical.entity.Consultation;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.common.exception.authorization.PermissionDeniedException;
import com.healthcare.hms.organization.entity.Doctor;
import com.healthcare.hms.organization.repository.DoctorRepository;
import com.healthcare.hms.security.principal.CurrentUser;
import com.healthcare.hms.security.util.SecurityUtils;
import com.healthcare.hms.users.constant.PermissionConstants;
import com.healthcare.hms.users.enums.RoleType;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Enforces consultation-module actor scopes (doctor-only access where applicable).
 */
@Component
public class ConsultationActorScopeSupport {

    private final DoctorRepository doctorRepository;

    public ConsultationActorScopeSupport(final DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public void denyPatientPortalStaffApis() {
        final CurrentUser user = SecurityUtils.requireCurrentUser();
        if (user.hasRole(RoleType.Names.PATIENT)) {
            throw new PermissionDeniedException(Set.of(PermissionConstants.VISIT_READ), false);
        }
    }

    public boolean isDoctorScoped() {
        final CurrentUser user = SecurityUtils.requireCurrentUser();
        if (!user.hasRole(RoleType.Names.DOCTOR)) {
            return false;
        }
        return !user.hasRole(RoleType.Names.SUPER_ADMIN)
                && !user.hasRole(RoleType.Names.HOSPITAL_ADMIN)
                && !user.hasRole(RoleType.Names.RECEPTIONIST);
    }

    public Optional<UUID> resolveScopedDoctorId(final UUID tenantId) {
        denyPatientPortalStaffApis();
        if (!isDoctorScoped()) {
            return Optional.empty();
        }
        return Optional.of(requireOwnDoctor(tenantId).getId());
    }

    public Doctor requireOwnDoctor(final UUID tenantId) {
        final UUID userId = SecurityUtils.requireCurrentUserId();
        return doctorRepository.findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for current user"));
    }

    public void assertDoctorAccessible(final UUID tenantId, final UUID doctorId) {
        denyPatientPortalStaffApis();
        final Optional<UUID> scoped = resolveScopedDoctorId(tenantId);
        if (scoped.isPresent() && !scoped.get().equals(doctorId)) {
            throw new PermissionDeniedException(Set.of(PermissionConstants.VISIT_READ), false);
        }
    }

    public void assertConsultationAccessible(final UUID tenantId, final Consultation consultation) {
        Objects.requireNonNull(consultation, "consultation");
        denyPatientPortalStaffApis();
        final Optional<UUID> scoped = resolveScopedDoctorId(tenantId);
        if (scoped.isPresent() && !scoped.get().equals(consultation.getDoctorId())) {
            throw new PermissionDeniedException(Set.of(PermissionConstants.VISIT_READ), false);
        }
    }

    public ConsultationSearchCriteria constrainSearch(
            final UUID tenantId,
            final ConsultationSearchCriteria criteria
    ) {
        denyPatientPortalStaffApis();
        final Optional<UUID> scoped = resolveScopedDoctorId(tenantId);
        if (scoped.isEmpty()) {
            return criteria;
        }
        final UUID ownDoctorId = scoped.get();
        if (criteria.doctorId() != null && !criteria.doctorId().equals(ownDoctorId)) {
            throw new PermissionDeniedException(Set.of(PermissionConstants.VISIT_READ), false);
        }
        return new ConsultationSearchCriteria(
                criteria.consultationNumber(),
                criteria.patientId(),
                criteria.patientName(),
                ownDoctorId,
                null,
                criteria.departmentId(),
                criteria.status(),
                criteria.fromDate(),
                criteria.toDate(),
                criteria.appointmentId()
        );
    }
}
