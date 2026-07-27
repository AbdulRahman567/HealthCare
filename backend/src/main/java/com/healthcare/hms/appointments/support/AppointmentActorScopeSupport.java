package com.healthcare.hms.appointments.support;

import com.healthcare.hms.appointments.calendar.enums.CalendarScope;
import com.healthcare.hms.appointments.dto.request.AppointmentSearchCriteria;
import com.healthcare.hms.appointments.entity.Appointment;
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
 * Enforces appointment-module actor scopes (Phase 6.9).
 *
 * <ul>
 *   <li>Patient portal roles cannot use staff appointment APIs (no self-scoped portal yet).</li>
 *   <li>Doctor-only actors (no admin/receptionist elevation) are limited to their own
 *       doctor profile for reads and mutations.</li>
 * </ul>
 */
@Component
public class AppointmentActorScopeSupport {

    private final DoctorRepository doctorRepository;

    public AppointmentActorScopeSupport(final DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    /**
     * Blocks the patient portal role from staff appointment surfaces until a dedicated
     * self-scoped patient portal API exists.
     */
    public void denyPatientPortalStaffApis() {
        final CurrentUser user = SecurityUtils.requireCurrentUser();
        if (user.hasRole(RoleType.Names.PATIENT)) {
            throw new PermissionDeniedException(Set.of(PermissionConstants.APPOINTMENT_READ), false);
        }
    }

    /**
     * {@code true} when the actor is a clinical doctor without hospital-wide ops elevation.
     */
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
            throw new PermissionDeniedException(Set.of(PermissionConstants.APPOINTMENT_READ), false);
        }
    }

    public void assertAppointmentAccessible(final UUID tenantId, final Appointment appointment) {
        Objects.requireNonNull(appointment, "appointment");
        denyPatientPortalStaffApis();
        final Optional<UUID> scoped = resolveScopedDoctorId(tenantId);
        if (scoped.isPresent() && !scoped.get().equals(appointment.getDoctorId())) {
            throw new PermissionDeniedException(Set.of(PermissionConstants.APPOINTMENT_READ), false);
        }
    }

    /**
     * Forces search to the actor's doctor when doctor-scoped; rejects conflicting filters.
     */
    public AppointmentSearchCriteria constrainSearch(
            final UUID tenantId,
            final AppointmentSearchCriteria criteria
    ) {
        denyPatientPortalStaffApis();
        final Optional<UUID> scoped = resolveScopedDoctorId(tenantId);
        if (scoped.isEmpty()) {
            return criteria;
        }
        final UUID ownDoctorId = scoped.get();
        if (criteria.doctorId() != null && !criteria.doctorId().equals(ownDoctorId)) {
            throw new PermissionDeniedException(Set.of(PermissionConstants.APPOINTMENT_READ), false);
        }
        return new AppointmentSearchCriteria(
                criteria.appointmentNumber(),
                criteria.patientId(),
                criteria.patientName(),
                ownDoctorId,
                null,
                criteria.departmentId(),
                criteria.departmentName(),
                criteria.status(),
                criteria.visitType(),
                criteria.fromDate(),
                criteria.toDate(),
                criteria.queueStatus()
        );
    }

    /**
     * Doctor-scoped actors may only use {@link CalendarScope#DOCTOR} for their own id.
     */
    public UUID constrainCalendarScopeId(
            final UUID tenantId,
            final CalendarScope scope,
            final UUID scopeId
    ) {
        denyPatientPortalStaffApis();
        final Optional<UUID> scoped = resolveScopedDoctorId(tenantId);
        if (scoped.isEmpty()) {
            return scopeId;
        }
        if (scope != CalendarScope.DOCTOR) {
            throw new PermissionDeniedException(Set.of(PermissionConstants.APPOINTMENT_READ), false);
        }
        if (scopeId != null && !scopeId.equals(scoped.get())) {
            throw new PermissionDeniedException(Set.of(PermissionConstants.APPOINTMENT_READ), false);
        }
        return scoped.get();
    }
}
