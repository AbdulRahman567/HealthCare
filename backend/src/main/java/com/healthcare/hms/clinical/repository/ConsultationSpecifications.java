package com.healthcare.hms.clinical.repository;

import com.healthcare.hms.clinical.dto.request.ConsultationSearchCriteria;
import com.healthcare.hms.clinical.entity.Consultation;
import com.healthcare.hms.clinical.enums.ConsultationStatus;
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
 * Database-level consultation search filters (Phase 7.2).
 * LIKE patterns escape {@code %}, {@code _}, and {@code \} (Phase 7.9).
 */
public final class ConsultationSpecifications {

    private ConsultationSpecifications() {
    }

    public static Specification<Consultation> withFilters(
            final UUID tenantId,
            final ConsultationSearchCriteria criteria
    ) {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));

            addConsultationNumber(predicates, cb, root, criteria.consultationNumber());

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

            final ConsultationStatus status = criteria.status();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (criteria.fromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("consultationDate"), criteria.fromDate()));
            }
            if (criteria.toDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("consultationDate"), criteria.toDate()));
            }

            if (criteria.appointmentId() != null) {
                predicates.add(cb.equal(root.get("appointmentId"), criteria.appointmentId()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static void addConsultationNumber(
            final List<Predicate> predicates,
            final CriteriaBuilder cb,
            final Root<Consultation> root,
            final String consultationNumber
    ) {
        if (!StringUtils.hasText(consultationNumber)) {
            return;
        }
        final String prefix = escapeLike(consultationNumber.trim().toUpperCase(Locale.ROOT)) + "%";
        predicates.add(cb.like(cb.upper(root.get("consultationNumber")), prefix, '\\'));
    }

    private static Predicate patientNameExists(
            final UUID tenantId,
            final String patientName,
            final Root<Consultation> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder cb
    ) {
        final Subquery<Long> subquery = query.subquery(Long.class);
        final Root<Patient> patientRoot = subquery.from(Patient.class);
        subquery.select(cb.literal(1L));
        subquery.where(
                cb.equal(patientRoot.get("tenantId"), tenantId),
                cb.equal(patientRoot.get("id"), root.get("patientId")),
                patientNameMatch(cb, patientRoot, patientName)
        );
        return cb.exists(subquery);
    }

    private static Predicate doctorNameExists(
            final UUID tenantId,
            final String doctorName,
            final Root<Consultation> root,
            final CriteriaQuery<?> query,
            final CriteriaBuilder cb
    ) {
        final Subquery<Long> subquery = query.subquery(Long.class);
        final Root<Doctor> doctorRoot = subquery.from(Doctor.class);
        final Root<User> userRoot = subquery.from(User.class);
        subquery.select(cb.literal(1L));
        subquery.where(
                cb.equal(doctorRoot.get("tenantId"), tenantId),
                cb.equal(doctorRoot.get("id"), root.get("doctorId")),
                cb.equal(userRoot.get("id"), doctorRoot.get("userId")),
                doctorNameMatch(cb, doctorRoot, userRoot, doctorName)
        );
        return cb.exists(subquery);
    }

    private static Predicate patientNameMatch(
            final CriteriaBuilder cb,
            final Root<Patient> patientRoot,
            final String name
    ) {
        final String pattern = "%" + escapeLike(name.toLowerCase(Locale.ROOT)) + "%";
        final Expression<String> fullName = cb.lower(cb.concat(
                cb.concat(patientRoot.get("firstName"), " "),
                patientRoot.get("lastName")
        ));
        return cb.or(
                cb.like(cb.lower(patientRoot.get("firstName")), pattern, '\\'),
                cb.like(cb.lower(patientRoot.get("lastName")), pattern, '\\'),
                cb.like(fullName, pattern, '\\'),
                cb.like(cb.lower(patientRoot.get("mrn")), pattern, '\\')
        );
    }

    private static Predicate doctorNameMatch(
            final CriteriaBuilder cb,
            final Root<Doctor> doctorRoot,
            final Root<User> userRoot,
            final String name
    ) {
        final String pattern = "%" + escapeLike(name.toLowerCase(Locale.ROOT)) + "%";
        final String codePrefix = escapeLike(name.toUpperCase(Locale.ROOT)) + "%";
        final Expression<String> fullName = cb.lower(cb.concat(
                cb.concat(userRoot.get("firstName"), " "),
                userRoot.get("lastName")
        ));
        return cb.or(
                cb.like(cb.lower(userRoot.get("firstName")), pattern, '\\'),
                cb.like(cb.lower(userRoot.get("lastName")), pattern, '\\'),
                cb.like(fullName, pattern, '\\'),
                cb.like(cb.upper(doctorRoot.get("employeeCode")), codePrefix, '\\')
        );
    }

    static String escapeLike(final String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
