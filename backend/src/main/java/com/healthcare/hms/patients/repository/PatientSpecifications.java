package com.healthcare.hms.patients.repository;

import com.healthcare.hms.patients.entity.Patient;
import com.healthcare.hms.patients.enums.BloodGroup;
import com.healthcare.hms.patients.enums.Gender;
import com.healthcare.hms.patients.enums.PatientStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * Database-level patient search filters (Phase 5.7).
 *
 * <p>Age filters are converted to {@code date_of_birth} range predicates so MySQL
 * can use {@code idx_patients_tenant_dob}. No in-memory filtering.
 */
public final class PatientSpecifications {

    private PatientSpecifications() {
    }

    public static Specification<Patient> withFilters(
            final UUID tenantId,
            final String q,
            final String mrn,
            final String firstName,
            final String lastName,
            final String phone,
            final String email,
            final String nationalId,
            final PatientStatus status,
            final BloodGroup bloodGroup,
            final Gender gender,
            final LocalDate dateOfBirth,
            final LocalDate dateOfBirthFrom,
            final LocalDate dateOfBirthTo,
            final Integer ageMin,
            final Integer ageMax,
            final UUID departmentId,
            final UUID doctorId,
            final LocalDate today
    ) {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));

            addEqualsIgnoreCase(predicates, cb, root, "mrn", mrn);
            addStartsWithIgnoreCase(predicates, cb, root, "firstName", firstName);
            addStartsWithIgnoreCase(predicates, cb, root, "lastName", lastName);
            addStartsWithIgnoreCase(predicates, cb, root, "phone", phone);
            addEqualsIgnoreCase(predicates, cb, root, "email", email);
            addEqualsIgnoreCase(predicates, cb, root, "nationalId", nationalId);

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (bloodGroup != null) {
                predicates.add(cb.equal(root.get("bloodGroup"), bloodGroup));
            }
            if (gender != null) {
                predicates.add(cb.equal(root.get("gender"), gender));
            }
            if (dateOfBirth != null) {
                predicates.add(cb.equal(root.get("dateOfBirth"), dateOfBirth));
            }
            if (dateOfBirthFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("dateOfBirth"), dateOfBirthFrom));
            }
            if (dateOfBirthTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("dateOfBirth"), dateOfBirthTo));
            }

            final LocalDate effectiveToday = today != null ? today : LocalDate.now();
            applyAgeRange(predicates, cb, root, ageMin, ageMax, effectiveToday);

            if (departmentId != null) {
                predicates.add(cb.equal(root.get("primaryDepartmentId"), departmentId));
            }
            if (doctorId != null) {
                predicates.add(cb.equal(root.get("primaryDoctorId"), doctorId));
            }

            if (StringUtils.hasText(q)) {
                predicates.add(freeTextPredicate(cb, root, q.trim()));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    /**
     * Converts age bounds into indexed {@code date_of_birth} predicates.
     *
     * <ul>
     *   <li>{@code ageMin = N} → dob ≤ today − N years</li>
     *   <li>{@code ageMax = M} → dob ≥ today − (M+1) years + 1 day</li>
     * </ul>
     */
    static void applyAgeRange(
            final List<Predicate> predicates,
            final CriteriaBuilder cb,
            final Root<Patient> root,
            final Integer ageMin,
            final Integer ageMax,
            final LocalDate today
    ) {
        if (ageMin != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("dateOfBirth"), today.minusYears(ageMin)));
        }
        if (ageMax != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                    root.get("dateOfBirth"),
                    today.minusYears(ageMax + 1L).plusDays(1)
            ));
        }
    }

    private static Predicate freeTextPredicate(
            final CriteriaBuilder cb,
            final Root<Patient> root,
            final String raw
    ) {
        final String escaped = escapeLike(raw.toLowerCase(Locale.ROOT));
        final String contains = "%" + escaped + "%";
        final String prefix = escaped + "%";

        final Expression<String> fullName = cb.lower(cb.concat(
                cb.concat(root.get("firstName"), " "),
                root.get("lastName")
        ));

        return cb.or(
                cb.like(cb.lower(root.get("mrn")), prefix, '\\'),
                cb.like(cb.lower(root.get("firstName")), contains, '\\'),
                cb.like(cb.lower(root.get("lastName")), contains, '\\'),
                cb.like(fullName, contains, '\\'),
                cb.like(cb.lower(root.get("phone")), prefix, '\\'),
                cb.like(cb.lower(root.get("email")), contains, '\\'),
                cb.like(cb.lower(root.get("nationalId")), prefix, '\\')
        );
    }

    private static void addEqualsIgnoreCase(
            final List<Predicate> predicates,
            final CriteriaBuilder cb,
            final Root<Patient> root,
            final String field,
            final String value
    ) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        predicates.add(cb.equal(cb.lower(root.get(field)), value.trim().toLowerCase(Locale.ROOT)));
    }

    private static void addStartsWithIgnoreCase(
            final List<Predicate> predicates,
            final CriteriaBuilder cb,
            final Root<Patient> root,
            final String field,
            final String value
    ) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        final String pattern = escapeLike(value.trim().toLowerCase(Locale.ROOT)) + "%";
        predicates.add(cb.like(cb.lower(root.get(field)), pattern, '\\'));
    }

    static String escapeLike(final String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
