package com.healthcare.hms.appointments.calendar.service;

import com.healthcare.hms.appointments.calendar.dto.response.CalendarMonthResponse;
import com.healthcare.hms.appointments.calendar.dto.response.CalendarRangeResponse;
import com.healthcare.hms.appointments.calendar.enums.CalendarScope;
import com.healthcare.hms.appointments.enums.AppointmentStatus;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface CalendarService {

    CalendarRangeResponse getDaily(
            CalendarScope scope,
            UUID scopeId,
            LocalDate date,
            AppointmentStatus status,
            Pageable pageable
    );

    CalendarRangeResponse getWeekly(
            CalendarScope scope,
            UUID scopeId,
            LocalDate dateInWeek,
            AppointmentStatus status,
            Pageable pageable
    );

    CalendarMonthResponse getMonthly(
            CalendarScope scope,
            UUID scopeId,
            int year,
            int month,
            AppointmentStatus status
    );
}
