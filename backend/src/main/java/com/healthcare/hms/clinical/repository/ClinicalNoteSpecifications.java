package com.healthcare.hms.clinical.repository;

import com.healthcare.hms.clinical.entity.ClinicalNote;
import com.healthcare.hms.clinical.enums.ClinicalNoteType;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Patient clinical-note history filters.
 */
public final class ClinicalNoteSpecifications {

    private ClinicalNoteSpecifications() {
    }

    public static Specification<ClinicalNote> forPatientHistory(
            final UUID tenantId,
            final UUID patientId,
            final ClinicalNoteType noteType,
            final LocalDate fromDate,
            final LocalDate toDate
    ) {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            predicates.add(cb.equal(root.get("patientId"), patientId));

            if (noteType != null) {
                predicates.add(cb.equal(root.get("noteType"), noteType));
            }
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
