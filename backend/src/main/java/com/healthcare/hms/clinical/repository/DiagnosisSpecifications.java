package com.healthcare.hms.clinical.repository;

import com.healthcare.hms.clinical.entity.Diagnosis;
import com.healthcare.hms.clinical.enums.DiagnosisStatus;
import com.healthcare.hms.clinical.enums.DiagnosisType;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Patient diagnosis history filters (cross-consultation queries).
 */
public final class DiagnosisSpecifications {

    private DiagnosisSpecifications() {
    }

    public static Specification<Diagnosis> forPatientHistory(
            final UUID tenantId,
            final UUID patientId,
            final DiagnosisType diagnosisType,
            final DiagnosisStatus diagnosisStatus,
            final LocalDate fromDate,
            final LocalDate toDate
    ) {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            predicates.add(cb.equal(root.get("patientId"), patientId));

            if (diagnosisType != null) {
                predicates.add(cb.equal(root.get("diagnosisType"), diagnosisType));
            }
            if (diagnosisStatus != null) {
                predicates.add(cb.equal(root.get("diagnosisStatus"), diagnosisStatus));
            }
            if (fromDate != null) {
                final Instant from = fromDate.atStartOfDay(ZoneOffset.UTC).toInstant();
                predicates.add(cb.greaterThanOrEqualTo(root.get("diagnosedAt"), from));
            }
            if (toDate != null) {
                final Instant to = toDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
                predicates.add(cb.lessThan(root.get("diagnosedAt"), to));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
