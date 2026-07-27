package com.healthcare.hms.appointments.calendar.service.impl;

import com.healthcare.hms.appointments.calendar.dto.response.CalendarDayResponse;
import com.healthcare.hms.appointments.calendar.dto.response.CalendarDaySummaryResponse;
import com.healthcare.hms.appointments.calendar.dto.response.CalendarEventResponse;
import com.healthcare.hms.appointments.calendar.dto.response.CalendarMonthResponse;
import com.healthcare.hms.appointments.calendar.dto.response.CalendarRangeResponse;
import com.healthcare.hms.appointments.calendar.enums.CalendarScope;
import com.healthcare.hms.appointments.calendar.enums.CalendarViewType;
import com.healthcare.hms.appointments.calendar.service.CalendarService;
import com.healthcare.hms.appointments.calendar.support.CalendarEventEnricher;
import com.healthcare.hms.appointments.entity.Appointment;
import com.healthcare.hms.appointments.enums.AppointmentStatus;
import com.healthcare.hms.appointments.repository.AppointmentRepository;
import com.healthcare.hms.appointments.support.AppointmentActorScopeSupport;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.hospitals.repository.HospitalRepository;
import com.healthcare.hms.organization.repository.DepartmentRepository;
import com.healthcare.hms.organization.repository.DoctorRepository;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.users.constant.PermissionConstants;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Optimized appointment calendars (Phase 6.5).
 *
 * <p>Query plan per request:
 * <ul>
 *   <li>DAILY/WEEKLY — 1 paginated appointment query + ≤4 batch lookups for labels</li>
 *   <li>MONTHLY — 1 {@code GROUP BY} aggregation query (no event hydration)</li>
 * </ul>
 * Existing indexes on {@code (tenant_id, doctor_id|department_id|hospital_id, appointment_date)}
 * back range scans.
 */
@Service
public class CalendarServiceImpl implements CalendarService {

    private static final int MAX_PAGE_SIZE = 200;
    private static final Set<String> ALLOWED_SORT = Set.of("appointmentDate", "startTime", "status");

    private final AppointmentRepository appointmentRepository;
    private final CalendarEventEnricher eventEnricher;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final HospitalRepository hospitalRepository;
    private final AppointmentActorScopeSupport actorScopeSupport;

    public CalendarServiceImpl(
            final AppointmentRepository appointmentRepository,
            final CalendarEventEnricher eventEnricher,
            final DoctorRepository doctorRepository,
            final DepartmentRepository departmentRepository,
            final HospitalRepository hospitalRepository,
            final AppointmentActorScopeSupport actorScopeSupport
    ) {
        this.appointmentRepository = appointmentRepository;
        this.eventEnricher = eventEnricher;
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
        this.hospitalRepository = hospitalRepository;
        this.actorScopeSupport = actorScopeSupport;
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    public CalendarRangeResponse getDaily(
            final CalendarScope scope,
            final UUID scopeId,
            final LocalDate date,
            final AppointmentStatus status,
            final Pageable pageable
    ) {
        final LocalDate day = date == null ? LocalDate.now() : date;
        return loadRange(scope, scopeId, CalendarViewType.DAILY, day, day, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    public CalendarRangeResponse getWeekly(
            final CalendarScope scope,
            final UUID scopeId,
            final LocalDate dateInWeek,
            final AppointmentStatus status,
            final Pageable pageable
    ) {
        final LocalDate anchor = dateInWeek == null ? LocalDate.now() : dateInWeek;
        final LocalDate from = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        final LocalDate to = from.plusDays(6);
        return loadRange(scope, scopeId, CalendarViewType.WEEKLY, from, to, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    public CalendarMonthResponse getMonthly(
            final CalendarScope scope,
            final UUID scopeId,
            final int year,
            final int month,
            final AppointmentStatus status
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        assertValidMonth(year, month);
        final UUID constrainedScopeId = actorScopeSupport.constrainCalendarScopeId(tenantId, scope, scopeId);
        final ScopeFilter filter = resolveScope(tenantId, scope, constrainedScopeId);
        final YearMonth yearMonth = YearMonth.of(year, month);
        final LocalDate from = yearMonth.atDay(1);
        final LocalDate to = yearMonth.atEndOfMonth();

        final List<Object[]> rows = appointmentRepository.aggregateCalendarByDateAndStatus(
                tenantId,
                from,
                to,
                filter.doctorId(),
                filter.departmentId(),
                filter.hospitalId(),
                status
        );

        final Map<LocalDate, Map<AppointmentStatus, Long>> byDate = new LinkedHashMap<>();
        long total = 0L;
        for (final Object[] row : rows) {
            final LocalDate date = (LocalDate) row[0];
            final AppointmentStatus rowStatus = (AppointmentStatus) row[1];
            final long count = (Long) row[2];
            byDate.computeIfAbsent(date, ignored -> new EnumMap<>(AppointmentStatus.class))
                    .merge(rowStatus, count, Long::sum);
            total += count;
        }

        final List<CalendarDaySummaryResponse> days = new ArrayList<>();
        for (LocalDate cursor = from; !cursor.isAfter(to); cursor = cursor.plusDays(1)) {
            final Map<AppointmentStatus, Long> counts =
                    byDate.getOrDefault(cursor, Map.of());
            final EnumMap<AppointmentStatus, Long> copy = new EnumMap<>(AppointmentStatus.class);
            copy.putAll(counts);
            final long dayTotal = counts.values().stream().mapToLong(Long::longValue).sum();
            days.add(new CalendarDaySummaryResponse(cursor, dayTotal, copy));
        }

        return new CalendarMonthResponse(scope, constrainedScopeId, year, month, total, days);
    }

    private CalendarRangeResponse loadRange(
            final CalendarScope scope,
            final UUID scopeId,
            final CalendarViewType view,
            final LocalDate from,
            final LocalDate to,
            final AppointmentStatus status,
            final Pageable pageable
    ) {
        final UUID tenantId = TenantContextHolder.requireTenantId();
        final UUID constrainedScopeId = actorScopeSupport.constrainCalendarScopeId(tenantId, scope, scopeId);
        final ScopeFilter filter = resolveScope(tenantId, scope, constrainedScopeId);
        final Pageable safePage = sanitizePageable(pageable);

        final Page<Appointment> page = appointmentRepository.findCalendarEvents(
                tenantId,
                from,
                to,
                filter.doctorId(),
                filter.departmentId(),
                filter.hospitalId(),
                status,
                safePage
        );

        final List<CalendarEventResponse> events = eventEnricher.enrich(tenantId, page.getContent());
        final Map<LocalDate, List<CalendarEventResponse>> eventsByDay = events.stream()
                .collect(Collectors.groupingBy(
                        CalendarEventResponse::appointmentDate,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // Full-range day counts for the header cells (one aggregation query — not N+1).
        final List<Object[]> aggregates = appointmentRepository.aggregateCalendarByDateAndStatus(
                tenantId,
                from,
                to,
                filter.doctorId(),
                filter.departmentId(),
                filter.hospitalId(),
                status
        );
        final Map<LocalDate, Map<AppointmentStatus, Long>> countsByDate = new LinkedHashMap<>();
        for (final Object[] row : aggregates) {
            final LocalDate date = (LocalDate) row[0];
            final AppointmentStatus rowStatus = (AppointmentStatus) row[1];
            final long count = (Long) row[2];
            countsByDate.computeIfAbsent(date, ignored -> new EnumMap<>(AppointmentStatus.class))
                    .merge(rowStatus, count, Long::sum);
        }

        final List<CalendarDayResponse> days = new ArrayList<>();
        for (LocalDate cursor = from; !cursor.isAfter(to); cursor = cursor.plusDays(1)) {
            final Map<AppointmentStatus, Long> counts =
                    countsByDate.getOrDefault(cursor, Map.of());
            final EnumMap<AppointmentStatus, Long> copy = new EnumMap<>(AppointmentStatus.class);
            copy.putAll(counts);
            final long dayTotal = counts.values().stream().mapToLong(Long::longValue).sum();
            days.add(new CalendarDayResponse(
                    cursor,
                    dayTotal,
                    copy,
                    eventsByDay.getOrDefault(cursor, List.of())
            ));
        }

        final PageResponse<CalendarEventResponse> eventPage = new PageResponse<>(
                events,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );

        return new CalendarRangeResponse(scope, constrainedScopeId, view, from, to, days, eventPage);
    }

    private ScopeFilter resolveScope(final UUID tenantId, final CalendarScope scope, final UUID scopeId) {
        if (scopeId == null) {
            throw new BusinessException("CALENDAR_SCOPE_REQUIRED", "Calendar scope id is required");
        }
        return switch (scope) {
            case DOCTOR -> {
                doctorRepository.findByIdAndTenantId(scopeId, tenantId)
                        .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
                yield new ScopeFilter(scopeId, null, null);
            }
            case DEPARTMENT -> {
                departmentRepository.findByIdAndTenantId(scopeId, tenantId)
                        .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
                yield new ScopeFilter(null, scopeId, null);
            }
            case HOSPITAL -> {
                hospitalRepository.findByIdAndTenantId(scopeId, tenantId)
                        .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));
                yield new ScopeFilter(null, null, scopeId);
            }
        };
    }

    private Pageable sanitizePageable(final Pageable pageable) {
        final int size = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);
        Sort sort = pageable.getSort();
        if (sort.isUnsorted()) {
            sort = Sort.by(Sort.Order.asc("appointmentDate"), Sort.Order.asc("startTime"));
        } else {
            final boolean allowed = sort.stream().allMatch(order -> ALLOWED_SORT.contains(order.getProperty()));
            if (!allowed) {
                throw new BusinessException(
                        "INVALID_SORT",
                        "Sort must be one of: " + String.join(", ", ALLOWED_SORT)
                );
            }
        }
        return PageRequest.of(Math.max(pageable.getPageNumber(), 0), size, sort);
    }

    private static void assertValidMonth(final int year, final int month) {
        if (year < 2000 || year > 2100) {
            throw new BusinessException("INVALID_YEAR", "Year must be between 2000 and 2100");
        }
        if (month < 1 || month > 12) {
            throw new BusinessException("INVALID_MONTH", "Month must be between 1 and 12");
        }
    }

    private record ScopeFilter(UUID doctorId, UUID departmentId, UUID hospitalId) {
    }
}
