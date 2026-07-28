package com.healthcare.hms.clinical.repository;

import com.healthcare.hms.clinical.entity.FollowUp;
import com.healthcare.hms.clinical.enums.FollowUpPriority;
import com.healthcare.hms.clinical.enums.FollowUpStatus;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Follow-up search / due-list / patient-history filters.
 */
public final class FollowUpSpecifications {

    private FollowUpSpecifications() {
    }

    public static Specification<FollowUp> forPatientHistory(
            final UUID tenantId,
            final UUID patientId,
            final FollowUpStatus status,
            final LocalDate fromDate,
            final LocalDate toDate
    ) {
        return search(tenantId, patientId, null, null, status, null, fromDate, toDate, false, false, null);
    }

    public static Specification<FollowUp> search(
            final UUID tenantId,
            final UUID patientId,
            final UUID doctorId,
            final UUID consultationId,
            final FollowUpStatus status,
            final FollowUpPriority priority,
            final LocalDate fromDate,
            final LocalDate toDate,
            final boolean overdueOnly,
            final boolean dueSoonOnly,
            final Integer dueWithinDays
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
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("scheduledDate"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("scheduledDate"), toDate));
            }

            final LocalDate today = LocalDate.now();
            if (overdueOnly) {
                predicates.add(root.get("status").in(List.of(FollowUpStatus.PENDING, FollowUpStatus.SCHEDULED)));
                predicates.add(cb.lessThan(root.get("scheduledDate"), today));
            }
            if (dueSoonOnly) {
                final int days = dueWithinDays != null && dueWithinDays > 0 ? dueWithinDays : 7;
                predicates.add(root.get("status").in(List.of(FollowUpStatus.PENDING, FollowUpStatus.SCHEDULED)));
                predicates.add(cb.greaterThanOrEqualTo(root.get("scheduledDate"), today));
                predicates.add(cb.lessThanOrEqualTo(root.get("scheduledDate"), today.plusDays(days)));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static Specification<FollowUp> dueForDoctor(
            final UUID tenantId,
            final UUID doctorId,
            final LocalDate asOfDate,
            final int withinDays,
            final Collection<FollowUpStatus> openStatuses
    ) {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            predicates.add(cb.equal(root.get("doctorId"), doctorId));
            predicates.add(root.get("status").in(openStatuses));
            predicates.add(cb.lessThanOrEqualTo(root.get("scheduledDate"), asOfDate.plusDays(withinDays)));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
