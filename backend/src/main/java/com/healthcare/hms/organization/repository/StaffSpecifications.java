package com.healthcare.hms.organization.repository;

import com.healthcare.hms.organization.enums.EmploymentStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * Shared list filters for staff specializations.
 */
public final class StaffSpecifications {

    private StaffSpecifications() {
    }

    public static <T> Specification<T> withFilters(
            final UUID tenantId,
            final String search,
            final EmploymentStatus employmentStatus,
            final UUID departmentId,
            final String... searchFields
    ) {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            if (employmentStatus != null) {
                predicates.add(cb.equal(root.get("employmentStatus"), employmentStatus));
            }
            if (departmentId != null) {
                predicates.add(cb.equal(root.get("departmentId"), departmentId));
            }
            if (StringUtils.hasText(search) && searchFields.length > 0) {
                final String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(buildSearchPredicates(root, cb, pattern, searchFields)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static <T> Predicate[] buildSearchPredicates(
            final Root<T> root,
            final CriteriaBuilder cb,
            final String pattern,
            final String[] searchFields
    ) {
        final Predicate[] predicates = new Predicate[searchFields.length];
        for (int i = 0; i < searchFields.length; i++) {
            predicates[i] = cb.like(cb.lower(root.get(searchFields[i])), pattern);
        }
        return predicates;
    }
}
