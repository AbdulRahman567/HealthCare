package com.healthcare.hms.appointments.repository;

import com.healthcare.hms.appointments.dto.request.AppointmentSearchCriteria;
import com.healthcare.hms.appointments.entity.Appointment;
import com.healthcare.hms.appointments.enums.AppointmentStatus;
import com.healthcare.hms.appointments.enums.VisitType;
import com.healthcare.hms.appointments.queue.entity.QueueEntry;
import com.healthcare.hms.appointments.queue.enums.QueueEntryStatus;
import com.healthcare.hms.organization.entity.Department;
import com.healthcare.hms.organization.entity.Doctor;
import com.healthcare.hms.patients.entity.Patient;
import com.healthcare.hms.users.entity.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * Database-level appointment search filters (Phase 6.6).
 *
 * <p>Cross-module name and queue filters use correlated {@code EXISTS} subqueries
 * so filtering stays in MySQL (no in-memory filtering). Indexed columns on
 * {@code appointments}, {@code patients}, {@code users}, {@code departments},
 * and {@code queue_entries} back the common predicates.
 */
public final class AppointmentSpecifications {

    private AppointmentSpecifications() {
    }

    public static Specification<Appointment> withFilters(
            final UUID tenantId,
            final AppointmentSearchCriteria criteria
    ) {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));

            addAppointmentNumber(predicates, cb, root, criteria.appointmentNumber());

            if (criteria.patientId() != null) {
                predicates.add(cb.equal(root.get("patientId"), criteria.patientId()));
            }
            if (StringUtils.hasText(criteria.patientName())) {
                predicates.add(patientNameExists(tenantId, criteria.patientName().trim(), root, query, cb));
            }

            if (criteria.doctorId() != null) {
                predicates.add(cb.equal(root.get("doctorId"), criteria.doctorId()));
            }
            if (StringUtils.hasText(criteria.doctorName())) {
                predicates.add(doctorNameExists(tenantId, criteria.doctorName().trim(), root, query, cb));
            }

            if (criteria.departmentId() != null) {
                predicates.add(cb.equal(root.get("departmentId"), criteria.departmentId()));
            }
            if (StringUtils.hasText(criteria.departmentName())) {
                predicates.add(departmentNameExists(tenantId, criteria.departmentName().trim(), root, query, cb));
            }

            final AppointmentStatus status = criteria.status();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            final VisitType visitType = criteria.visitType();
            if (visitType != null) {
                predicates.add(cb.equal(root.get("visitType"), visitType));
            }

            if (criteria.fromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("appointmentDate"), criteria.fromDate()));
            }
            if (criteria.toDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("appointmentDate"), criteria.toDate()));
            }

            final QueueEntryStatus queueStatus = criteria.queueStatus();
            if (queueStatus != null) {
                predicates.add(queueStatusExists(tenantId, queueStatus, root, query, cb));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    /**
     * Backward-compatible overload used by calendar / legacy callers.
     */
    public static Specification<Appointment> withFilters(
            final UUID tenantId,
            final UUID patientId,
            final UUID doctorId,
            final AppointmentStatus status,
            final java.time.LocalDate fromDate,
            final java.time.LocalDate toDate
    ) {
        return withFilters(
                tenantId,
                new AppointmentSearchCriteria(
                        null,
                        patientId,
                        null,
                        doctorId,
                        null,
                        null,
                        null,
                        status,
                        null,
                        fromDate,
                        toDate,
                        null
                )
        );
    }

    private static void addAppointmentNumber(
            final List<Predicate> predicates,
            final CriteriaBuilder cb,
            final Root<Appointment> root,
            final String appointmentNumber
    ) {
        if (!StringUtils.hasText(appointmentNumber)) {
            return;
        }
        final String pattern = escapeLike(appointmentNumber.trim().toLowerCase(Locale.ROOT)) + "%";
        predicates.add(cb.like(cb.lower(root.get("appointmentNumber")), pattern, '\\'));
    }

    private static Predicate patientNameExists(
            final UUID tenantId,
            final String patientName,
            final Root<Appointment> appointment,
            final CriteriaQuery<?> query,
            final CriteriaBuilder cb
    ) {
        final Subquery<Integer> sub = query.subquery(Integer.class);
        final Root<Patient> patient = sub.from(Patient.class);
        sub.select(cb.literal(1));
        sub.where(
                cb.equal(patient.get("id"), appointment.get("patientId")),
                cb.equal(patient.get("tenantId"), tenantId),
                personNameMatch(cb, patient.get("firstName"), patient.get("lastName"), patientName)
        );
        return cb.exists(sub);
    }

    private static Predicate doctorNameExists(
            final UUID tenantId,
            final String doctorName,
            final Root<Appointment> appointment,
            final CriteriaQuery<?> query,
            final CriteriaBuilder cb
    ) {
        final Subquery<Integer> sub = query.subquery(Integer.class);
        final Root<Doctor> doctor = sub.from(Doctor.class);
        final Root<User> user = sub.from(User.class);
        sub.select(cb.literal(1));
        sub.where(
                cb.equal(doctor.get("id"), appointment.get("doctorId")),
                cb.equal(doctor.get("tenantId"), tenantId),
                cb.equal(user.get("id"), doctor.get("userId")),
                cb.equal(user.get("tenantId"), tenantId),
                cb.or(
                        personNameMatch(cb, user.get("firstName"), user.get("lastName"), doctorName),
                        startsWithIgnoreCase(cb, doctor.get("employeeCode"), doctorName)
                )
        );
        return cb.exists(sub);
    }

    private static Predicate departmentNameExists(
            final UUID tenantId,
            final String departmentName,
            final Root<Appointment> appointment,
            final CriteriaQuery<?> query,
            final CriteriaBuilder cb
    ) {
        final Subquery<Integer> sub = query.subquery(Integer.class);
        final Root<Department> department = sub.from(Department.class);
        sub.select(cb.literal(1));
        final String pattern = "%" + escapeLike(departmentName.toLowerCase(Locale.ROOT)) + "%";
        sub.where(
                cb.equal(department.get("id"), appointment.get("departmentId")),
                cb.equal(department.get("tenantId"), tenantId),
                cb.or(
                        cb.like(cb.lower(department.get("name")), pattern, '\\'),
                        startsWithIgnoreCase(cb, department.get("code"), departmentName)
                )
        );
        return cb.exists(sub);
    }

    private static Predicate queueStatusExists(
            final UUID tenantId,
            final QueueEntryStatus queueStatus,
            final Root<Appointment> appointment,
            final CriteriaQuery<?> query,
            final CriteriaBuilder cb
    ) {
        final Subquery<Integer> sub = query.subquery(Integer.class);
        final Root<QueueEntry> entry = sub.from(QueueEntry.class);
        sub.select(cb.literal(1));
        sub.where(
                cb.equal(entry.get("appointmentId"), appointment.get("id")),
                cb.equal(entry.get("tenantId"), tenantId),
                cb.equal(entry.get("status"), queueStatus)
        );
        return cb.exists(sub);
    }

    private static Predicate personNameMatch(
            final CriteriaBuilder cb,
            final Expression<String> firstName,
            final Expression<String> lastName,
            final String raw
    ) {
        final String escaped = escapeLike(raw.toLowerCase(Locale.ROOT));
        final String contains = "%" + escaped + "%";
        final Expression<String> fullName = cb.lower(cb.concat(cb.concat(firstName, " "), lastName));
        return cb.or(
                cb.like(cb.lower(firstName), contains, '\\'),
                cb.like(cb.lower(lastName), contains, '\\'),
                cb.like(fullName, contains, '\\')
        );
    }

    private static Predicate startsWithIgnoreCase(
            final CriteriaBuilder cb,
            final Expression<String> field,
            final String value
    ) {
        final String pattern = escapeLike(value.toLowerCase(Locale.ROOT)) + "%";
        return cb.like(cb.lower(field), pattern, '\\');
    }

    static String escapeLike(final String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
