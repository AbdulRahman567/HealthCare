package com.healthcare.hms.prescriptions.repository;

import com.healthcare.hms.prescriptions.entity.Prescription;
import com.healthcare.hms.prescriptions.enums.PrescriptionStatus;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Prescription search / patient-history filters.
 */
public final class PrescriptionSpecifications {

    private PrescriptionSpecifications() {
    }

    public static Specification<Prescription> search(
            final UUID tenantId,
            final UUID patientId,
            final UUID doctorId,
            final UUID consultationId,
            final PrescriptionStatus status,
            final LocalDate fromDate,
            final LocalDate toDate,
            final String prescriptionNumber
    ) {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));

            if (patientId != null) {
                predicates.add(cb.equal(root.get("patientId"), patientId));
            }
            if (doctorId != null) {
                predicates.add(cb.equal(root.get("doctorId"), doctorId));
            }
            if (consultationId != null) {
                predicates.add(cb.equal(root.get("consultationId"), consultationId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("prescriptionDate"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("prescriptionDate"), toDate));
            }
            if (prescriptionNumber != null && !prescriptionNumber.isBlank()) {
                final String pattern = "%" + escapeLike(prescriptionNumber.trim().toLowerCase()) + "%";
                predicates.add(cb.like(cb.lower(root.get("prescriptionNumber")), pattern, '\\'));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    static String escapeLike(final String value) {
        return value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
