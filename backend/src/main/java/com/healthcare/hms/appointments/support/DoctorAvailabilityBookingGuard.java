package com.healthcare.hms.appointments.support;

import com.healthcare.hms.appointments.availability.entity.DoctorSchedule;
import com.healthcare.hms.appointments.availability.entity.DoctorScheduleBreak;
import com.healthcare.hms.appointments.availability.entity.DoctorScheduleWindow;
import com.healthcare.hms.appointments.availability.entity.DoctorUnavailability;
import com.healthcare.hms.appointments.availability.repository.DoctorScheduleBreakRepository;
import com.healthcare.hms.appointments.availability.repository.DoctorScheduleRepository;
import com.healthcare.hms.appointments.availability.repository.DoctorScheduleWindowRepository;
import com.healthcare.hms.appointments.availability.repository.DoctorUnavailabilityRepository;
import com.healthcare.hms.appointments.repository.AppointmentRepository;
import com.healthcare.hms.common.exception.BusinessException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Validates that a booking falls inside the doctor's published availability
 * (working window, outside breaks, outside leave/holiday/emergency, under daily max).
 */
@Component
public class DoctorAvailabilityBookingGuard {

    private final DoctorScheduleRepository scheduleRepository;
    private final DoctorScheduleWindowRepository windowRepository;
    private final DoctorScheduleBreakRepository breakRepository;
    private final DoctorUnavailabilityRepository unavailabilityRepository;
    private final AppointmentRepository appointmentRepository;

    public DoctorAvailabilityBookingGuard(
            final DoctorScheduleRepository scheduleRepository,
            final DoctorScheduleWindowRepository windowRepository,
            final DoctorScheduleBreakRepository breakRepository,
            final DoctorUnavailabilityRepository unavailabilityRepository,
            final AppointmentRepository appointmentRepository
    ) {
        this.scheduleRepository = scheduleRepository;
        this.windowRepository = windowRepository;
        this.breakRepository = breakRepository;
        this.unavailabilityRepository = unavailabilityRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public void assertSlotAvailable(
            final UUID tenantId,
            final UUID doctorId,
            final LocalDate date,
            final LocalTime startTime,
            final LocalTime endTime,
            final UUID excludeAppointmentId
    ) {
        assertNotUnavailable(tenantId, doctorId, date, startTime, endTime);

        final List<DoctorSchedule> schedules =
                scheduleRepository.findActiveCoveringDate(tenantId, doctorId, date);
        if (schedules.isEmpty()) {
            throw new BusinessException(
                    "DOCTOR_NO_SCHEDULE",
                    "Doctor has no active availability schedule covering this date"
            );
        }

        final DoctorSchedule schedule = schedules.getFirst();
        final DayOfWeek day = date.getDayOfWeek();
        final List<DoctorScheduleWindow> windows =
                windowRepository.findByTenantIdAndScheduleIdOrderByDayOfWeekAscStartTimeAsc(
                        tenantId, schedule.getId());
        final boolean insideWindow = windows.stream()
                .filter(window -> window.getDayOfWeek() == day)
                .anyMatch(window ->
                        !startTime.isBefore(window.getStartTime()) && !endTime.isAfter(window.getEndTime()));
        if (!insideWindow) {
            throw new BusinessException(
                    "OUTSIDE_WORKING_HOURS",
                    "Appointment is outside the doctor's working hours for " + day
            );
        }

        final List<DoctorScheduleBreak> breaks =
                breakRepository.findByTenantIdAndScheduleIdOrderByDayOfWeekAscStartTimeAsc(
                        tenantId, schedule.getId());
        final boolean overlapsBreak = breaks.stream()
                .filter(brk -> brk.getDayOfWeek() == day)
                .anyMatch(brk -> startTime.isBefore(brk.getEndTime()) && brk.getStartTime().isBefore(endTime));
        if (overlapsBreak) {
            throw new BusinessException(
                    "DOCTOR_BREAK_CONFLICT",
                    "Appointment overlaps a scheduled break for the doctor"
            );
        }

        long activeCount = appointmentRepository.countByTenantIdAndDoctorIdAndAppointmentDateAndStatusIn(
                tenantId, doctorId, date, AppointmentConflictGuard.ACTIVE_SLOT_STATUSES);
        if (excludeAppointmentId != null) {
            // Reschedule/update on same day should not count the appointment against itself.
            final boolean selfOnSameDay = appointmentRepository.findByIdAndTenantId(excludeAppointmentId, tenantId)
                    .filter(a -> a.getDoctorId().equals(doctorId) && a.getAppointmentDate().equals(date))
                    .filter(a -> AppointmentConflictGuard.ACTIVE_SLOT_STATUSES.contains(a.getStatus()))
                    .isPresent();
            if (selfOnSameDay) {
                activeCount = Math.max(0, activeCount - 1);
            }
        }
        if (activeCount >= schedule.getMaxAppointmentsPerDay()) {
            throw new BusinessException(
                    "DOCTOR_DAILY_CAPACITY",
                    "Doctor has reached the maximum appointments for this day ("
                            + schedule.getMaxAppointmentsPerDay() + ")"
            );
        }
    }

    private void assertNotUnavailable(
            final UUID tenantId,
            final UUID doctorId,
            final LocalDate date,
            final LocalTime startTime,
            final LocalTime endTime
    ) {
        final List<DoctorUnavailability> blocks = unavailabilityRepository.findOverlapping(
                tenantId, doctorId, date, date, null);
        for (final DoctorUnavailability block : blocks) {
            if (block.isAllDay()) {
                throw new BusinessException(
                        "DOCTOR_UNAVAILABLE",
                        "Doctor is unavailable (" + block.getUnavailabilityType() + ") on this date"
                );
            }
            if (block.getStartDate().equals(date)
                    && startTime.isBefore(block.getEndTime())
                    && block.getStartTime().isBefore(endTime)) {
                throw new BusinessException(
                        "DOCTOR_UNAVAILABLE",
                        "Doctor is unavailable (" + block.getUnavailabilityType() + ") during this time"
                );
            }
        }
    }
}
