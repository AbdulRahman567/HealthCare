package com.healthcare.hms.clinical.repository;

import com.healthcare.hms.clinical.entity.VitalSigns;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Patient vital-signs history filters (time-series queries).
 */
public final class VitalSignsSpecifications {

    private VitalSignsSpecifications() {
    }

    public static Specification<VitalSigns> forPatientHistory(
            final UUID tenantId,
            final UUID patientId,
            final LocalDate fromDate,
            final LocalDate toDate
    ) {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            predicates.add(cb.equal(root.get("patientId"), patientId));

            if (fromDate != null) {
                final Instant from = fromDate.atStartOfDay(ZoneOffset.UTC).toInstant();
                predicates.add(cb.greaterThanOrEqualTo(root.get("recordedAt"), from));
            }
            if (toDate != null) {
                final Instant to = toDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
                predicates.add(cb.lessThan(root.get("recordedAt"), to));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
