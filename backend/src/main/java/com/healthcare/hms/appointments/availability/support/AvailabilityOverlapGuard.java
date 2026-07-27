package com.healthcare.hms.appointments.availability.support;

import com.healthcare.hms.appointments.availability.entity.DoctorUnavailability;
import com.healthcare.hms.appointments.availability.enums.ScheduleStatus;
import com.healthcare.hms.appointments.availability.repository.DoctorScheduleRepository;
import com.healthcare.hms.appointments.availability.repository.DoctorUnavailabilityRepository;
import com.healthcare.hms.common.exception.ConflictException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Prevents overlapping ACTIVE schedule periods and overlapping unavailability blocks.
 *
 * <p>ACTIVE schedules for one doctor must have non-overlapping effective date ranges
 * so future recurring periods can be sequenced (e.g. Jan–Jun then Jul–Dec).
 */
@Component
public class AvailabilityOverlapGuard {

    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorUnavailabilityRepository unavailabilityRepository;

    public AvailabilityOverlapGuard(
            final DoctorScheduleRepository scheduleRepository,
            final DoctorUnavailabilityRepository unavailabilityRepository
    ) {
        this.scheduleRepository = scheduleRepository;
        this.unavailabilityRepository = unavailabilityRepository;
    }

    public void assertNoOverlappingActiveSchedule(
            final UUID tenantId,
            final UUID doctorId,
            final LocalDate effectiveFrom,
            final LocalDate effectiveTo,
            final ScheduleStatus status,
            final UUID excludeScheduleId
    ) {
        if (status != ScheduleStatus.ACTIVE) {
            return;
        }
        final List<?> overlapping = scheduleRepository.findOverlappingActiveSchedules(
                tenantId, doctorId, effectiveFrom, effectiveTo, excludeScheduleId);
        if (!overlapping.isEmpty()) {
            throw new ConflictException(
                    "DOCTOR_AVAILABILITY_OVERLAP",
                    "Active schedule effective dates overlap an existing schedule for this doctor"
            );
        }
    }

    public void assertNoOverlappingUnavailability(
            final UUID tenantId,
            final UUID doctorId,
            final LocalDate startDate,
            final LocalDate endDate,
            final boolean allDay,
            final LocalTime startTime,
            final LocalTime endTime,
            final UUID excludeId
    ) {
        final List<DoctorUnavailability> candidates = unavailabilityRepository.findOverlapping(
                tenantId, doctorId, startDate, endDate, excludeId);
        for (final DoctorUnavailability existing : candidates) {
            if (blocksOverlap(allDay, startDate, endDate, startTime, endTime, existing)) {
                throw new ConflictException(
                        "DOCTOR_UNAVAILABILITY_OVERLAP",
                        "Unavailability overlaps an existing leave, holiday, or emergency block"
                );
            }
        }
    }

    private static boolean blocksOverlap(
            final boolean allDay,
            final LocalDate startDate,
            final LocalDate endDate,
            final LocalTime startTime,
            final LocalTime endTime,
            final DoctorUnavailability existing
    ) {
        if (allDay || existing.isAllDay()) {
            return true;
        }
        if (!startDate.equals(existing.getStartDate())) {
            return false;
        }
        return startTime.isBefore(existing.getEndTime()) && existing.getStartTime().isBefore(endTime);
    }
}
