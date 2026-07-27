package com.healthcare.hms.appointments.availability.validation;

import com.healthcare.hms.appointments.availability.dto.request.ScheduleBreakRequest;
import com.healthcare.hms.appointments.availability.dto.request.ScheduleWindowRequest;
import com.healthcare.hms.appointments.availability.dto.request.UpsertDoctorScheduleRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Structural schedule rules: date range, window/break time order, no window overlaps,
 * breaks inside windows, no break overlaps.
 */
public class ValidDoctorScheduleRequestValidator
        implements ConstraintValidator<ValidDoctorScheduleRequest, UpsertDoctorScheduleRequest> {

    @Override
    public boolean isValid(final UpsertDoctorScheduleRequest request, final ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        boolean valid = true;

        if (request.effectiveFrom() != null
                && request.effectiveTo() != null
                && request.effectiveTo().isBefore(request.effectiveFrom())) {
            context.buildConstraintViolationWithTemplate("effectiveTo must be on or after effectiveFrom")
                    .addPropertyNode("effectiveTo")
                    .addConstraintViolation();
            valid = false;
        }

        final List<ScheduleWindowRequest> windows =
                request.windows() == null ? List.of() : request.windows();
        final Map<DayOfWeek, List<ScheduleWindowRequest>> windowsByDay = new EnumMap<>(DayOfWeek.class);
        for (int i = 0; i < windows.size(); i++) {
            final ScheduleWindowRequest window = windows.get(i);
            if (window == null || window.dayOfWeek() == null || window.startTime() == null || window.endTime() == null) {
                continue;
            }
            if (!window.endTime().isAfter(window.startTime())) {
                context.buildConstraintViolationWithTemplate("Window endTime must be after startTime")
                        .addPropertyNode("windows")
                        .addPropertyNode("endTime")
                        .inIterable().atIndex(i)
                        .addConstraintViolation();
                valid = false;
            }
            windowsByDay.computeIfAbsent(window.dayOfWeek(), ignored -> new ArrayList<>()).add(window);
        }

        for (final Map.Entry<DayOfWeek, List<ScheduleWindowRequest>> entry : windowsByDay.entrySet()) {
            final List<ScheduleWindowRequest> dayWindows = entry.getValue().stream()
                    .sorted(Comparator.comparing(ScheduleWindowRequest::startTime))
                    .toList();
            for (int i = 1; i < dayWindows.size(); i++) {
                final ScheduleWindowRequest previous = dayWindows.get(i - 1);
                final ScheduleWindowRequest current = dayWindows.get(i);
                if (timesOverlap(previous.startTime(), previous.endTime(), current.startTime(), current.endTime())) {
                    context.buildConstraintViolationWithTemplate(
                                    "Working hours overlap on " + entry.getKey())
                            .addPropertyNode("windows")
                            .addConstraintViolation();
                    valid = false;
                    break;
                }
            }
        }

        final List<ScheduleBreakRequest> breaks =
                request.breaks() == null ? List.of() : request.breaks();
        final Map<DayOfWeek, List<ScheduleBreakRequest>> breaksByDay = new EnumMap<>(DayOfWeek.class);
        for (int i = 0; i < breaks.size(); i++) {
            final ScheduleBreakRequest brk = breaks.get(i);
            if (brk == null || brk.dayOfWeek() == null || brk.startTime() == null || brk.endTime() == null) {
                continue;
            }
            if (!brk.endTime().isAfter(brk.startTime())) {
                context.buildConstraintViolationWithTemplate("Break endTime must be after startTime")
                        .addPropertyNode("breaks")
                        .addPropertyNode("endTime")
                        .inIterable().atIndex(i)
                        .addConstraintViolation();
                valid = false;
            }
            final List<ScheduleWindowRequest> dayWindows = windowsByDay.getOrDefault(brk.dayOfWeek(), List.of());
            final boolean insideWindow = dayWindows.stream().anyMatch(window ->
                    !brk.startTime().isBefore(window.startTime()) && !brk.endTime().isAfter(window.endTime()));
            if (!insideWindow) {
                context.buildConstraintViolationWithTemplate(
                                "Break must fall entirely within a working-hours window on " + brk.dayOfWeek())
                        .addPropertyNode("breaks")
                        .inIterable().atIndex(i)
                        .addConstraintViolation();
                valid = false;
            }
            breaksByDay.computeIfAbsent(brk.dayOfWeek(), ignored -> new ArrayList<>()).add(brk);
        }

        for (final Map.Entry<DayOfWeek, List<ScheduleBreakRequest>> entry : breaksByDay.entrySet()) {
            final List<ScheduleBreakRequest> dayBreaks = entry.getValue().stream()
                    .sorted(Comparator.comparing(ScheduleBreakRequest::startTime))
                    .toList();
            for (int i = 1; i < dayBreaks.size(); i++) {
                final ScheduleBreakRequest previous = dayBreaks.get(i - 1);
                final ScheduleBreakRequest current = dayBreaks.get(i);
                if (timesOverlap(previous.startTime(), previous.endTime(), current.startTime(), current.endTime())) {
                    context.buildConstraintViolationWithTemplate(
                                    "Break times overlap on " + entry.getKey())
                            .addPropertyNode("breaks")
                            .addConstraintViolation();
                    valid = false;
                    break;
                }
            }
        }

        return valid;
    }

    private static boolean timesOverlap(
            final LocalTime startA,
            final LocalTime endA,
            final LocalTime startB,
            final LocalTime endB
    ) {
        return startA.isBefore(endB) && startB.isBefore(endA);
    }
}
