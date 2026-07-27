package com.healthcare.hms.appointments.availability.mapper;

import com.healthcare.hms.appointments.availability.dto.request.ScheduleBreakRequest;
import com.healthcare.hms.appointments.availability.dto.request.ScheduleWindowRequest;
import com.healthcare.hms.appointments.availability.dto.request.UpsertDoctorScheduleRequest;
import com.healthcare.hms.appointments.availability.dto.request.UpsertDoctorUnavailabilityRequest;
import com.healthcare.hms.appointments.availability.dto.response.DoctorScheduleResponse;
import com.healthcare.hms.appointments.availability.dto.response.DoctorUnavailabilityResponse;
import com.healthcare.hms.appointments.availability.dto.response.ScheduleBreakResponse;
import com.healthcare.hms.appointments.availability.dto.response.ScheduleWindowResponse;
import com.healthcare.hms.appointments.availability.entity.DoctorSchedule;
import com.healthcare.hms.appointments.availability.entity.DoctorScheduleBreak;
import com.healthcare.hms.appointments.availability.entity.DoctorScheduleWindow;
import com.healthcare.hms.appointments.availability.entity.DoctorUnavailability;
import com.healthcare.hms.appointments.availability.enums.ScheduleRecurrenceType;
import com.healthcare.hms.appointments.availability.enums.ScheduleStatus;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DoctorAvailabilityMapper {

    public void applySchedule(final UpsertDoctorScheduleRequest request, final DoctorSchedule schedule) {
        schedule.setName(trimToNull(request.name()));
        schedule.setEffectiveFrom(request.effectiveFrom());
        schedule.setEffectiveTo(request.effectiveTo());
        schedule.setMaxAppointmentsPerDay(request.maxAppointmentsPerDay());
        schedule.setRecurrenceType(
                request.recurrenceType() != null ? request.recurrenceType() : ScheduleRecurrenceType.WEEKLY);
        schedule.setStatus(request.status() != null ? request.status() : ScheduleStatus.ACTIVE);
        schedule.setNotes(trimToNull(request.notes()));
    }

    public DoctorScheduleWindow toWindow(
            final ScheduleWindowRequest request,
            final DoctorSchedule schedule
    ) {
        final DoctorScheduleWindow window = new DoctorScheduleWindow();
        window.setScheduleId(schedule.getId());
        window.setDoctorId(schedule.getDoctorId());
        window.setDayOfWeek(request.dayOfWeek());
        window.setStartTime(request.startTime());
        window.setEndTime(request.endTime());
        return window;
    }

    public DoctorScheduleBreak toBreak(
            final ScheduleBreakRequest request,
            final DoctorSchedule schedule
    ) {
        final DoctorScheduleBreak brk = new DoctorScheduleBreak();
        brk.setScheduleId(schedule.getId());
        brk.setDoctorId(schedule.getDoctorId());
        brk.setDayOfWeek(request.dayOfWeek());
        brk.setStartTime(request.startTime());
        brk.setEndTime(request.endTime());
        brk.setLabel(trimToNull(request.label()));
        return brk;
    }

    public void applyUnavailability(
            final UpsertDoctorUnavailabilityRequest request,
            final DoctorUnavailability entity
    ) {
        entity.setUnavailabilityType(request.unavailabilityType());
        entity.setStartDate(request.startDate());
        entity.setEndDate(request.endDate());
        entity.setAllDay(Boolean.TRUE.equals(request.allDay()));
        if (entity.isAllDay()) {
            entity.setStartTime(null);
            entity.setEndTime(null);
        } else {
            entity.setStartTime(request.startTime());
            entity.setEndTime(request.endTime());
        }
        entity.setReason(trimToNull(request.reason()));
    }

    public DoctorScheduleResponse toScheduleResponse(
            final DoctorSchedule schedule,
            final List<DoctorScheduleWindow> windows,
            final List<DoctorScheduleBreak> breaks
    ) {
        return new DoctorScheduleResponse(
                schedule.getId(),
                schedule.getDoctorId(),
                schedule.getHospitalId(),
                schedule.getName(),
                schedule.getEffectiveFrom(),
                schedule.getEffectiveTo(),
                schedule.getMaxAppointmentsPerDay(),
                schedule.getRecurrenceType(),
                schedule.getStatus(),
                schedule.getNotes(),
                windows.stream().map(this::toWindowResponse).toList(),
                breaks.stream().map(this::toBreakResponse).toList(),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt(),
                schedule.getVersion()
        );
    }

    public ScheduleWindowResponse toWindowResponse(final DoctorScheduleWindow window) {
        return new ScheduleWindowResponse(
                window.getId(),
                window.getDayOfWeek(),
                window.getStartTime(),
                window.getEndTime(),
                window.getCreatedAt(),
                window.getUpdatedAt(),
                window.getVersion()
        );
    }

    public ScheduleBreakResponse toBreakResponse(final DoctorScheduleBreak brk) {
        return new ScheduleBreakResponse(
                brk.getId(),
                brk.getDayOfWeek(),
                brk.getStartTime(),
                brk.getEndTime(),
                brk.getLabel(),
                brk.getCreatedAt(),
                brk.getUpdatedAt(),
                brk.getVersion()
        );
    }

    public DoctorUnavailabilityResponse toUnavailabilityResponse(final DoctorUnavailability entity) {
        return new DoctorUnavailabilityResponse(
                entity.getId(),
                entity.getDoctorId(),
                entity.getHospitalId(),
                entity.getUnavailabilityType(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.isAllDay(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }

    private static String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
