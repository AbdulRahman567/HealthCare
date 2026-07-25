package com.healthcare.hms.organization.repository;

import com.healthcare.hms.organization.entity.Department;
import com.healthcare.hms.organization.enums.DepartmentStatus;
import com.healthcare.hms.organization.enums.DepartmentType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * Dynamic filters for department list / search.
 */
public final class DepartmentSpecifications {

    private DepartmentSpecifications() {
    }

    public static Specification<Department> withFilters(
            final UUID tenantId,
            final String search,
            final DepartmentStatus status,
            final DepartmentType departmentType,
            final UUID hospitalId
    ) {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (departmentType != null) {
                predicates.add(cb.equal(root.get("departmentType"), departmentType));
            }
            if (hospitalId != null) {
                predicates.add(cb.equal(root.get("hospitalId"), hospitalId));
            }
            if (StringUtils.hasText(search)) {
                final String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("code")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(root.get("location")), pattern)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
