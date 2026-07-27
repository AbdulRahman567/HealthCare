package com.healthcare.hms.appointments.calendar.controller;

import com.healthcare.hms.appointments.calendar.dto.response.CalendarMonthResponse;
import com.healthcare.hms.appointments.calendar.dto.response.CalendarRangeResponse;
import com.healthcare.hms.appointments.calendar.enums.CalendarScope;
import com.healthcare.hms.appointments.calendar.service.CalendarService;
import com.healthcare.hms.appointments.enums.AppointmentStatus;
import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.users.constant.PermissionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Appointment calendar APIs (Phase 6.5) — doctor, department, and hospital scopes.
 *
 * <p>Daily/weekly responses use one paginated appointment query plus batch label
 * enrichment. Monthly uses a single {@code GROUP BY} aggregation.
 */
@RestController
@RequestMapping("/api/v1/calendars")
@Tag(name = "Calendars", description = "Daily, weekly, and monthly appointment calendars")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(final CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    // --- Doctor ---

    @GetMapping("/doctors/{doctorId}/daily")
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    @Operation(summary = "Doctor daily calendar", description = "Paginated events for one doctor on one date.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    content = @Content(schema = @Schema(implementation = CalendarRangeResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<CalendarRangeResponse>> doctorDaily(
            @PathVariable("doctorId") final UUID doctorId,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date,
            @RequestParam(value = "status", required = false) final AppointmentStatus status,
            @PageableDefault(size = 50, sort = {"appointmentDate", "startTime"}, direction = Sort.Direction.ASC)
            final Pageable pageable
    ) {
        return ok(calendarService.getDaily(CalendarScope.DOCTOR, doctorId, date, status, pageable));
    }

    @GetMapping("/doctors/{doctorId}/weekly")
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    @Operation(summary = "Doctor weekly calendar", description = "ISO week (Mon–Sun) containing the given date.")
    public ResponseEntity<ApiResponse<CalendarRangeResponse>> doctorWeekly(
            @PathVariable("doctorId") final UUID doctorId,
            @Parameter(description = "Any date within the desired ISO week")
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date,
            @RequestParam(value = "status", required = false) final AppointmentStatus status,
            @PageableDefault(size = 100, sort = {"appointmentDate", "startTime"}, direction = Sort.Direction.ASC)
            final Pageable pageable
    ) {
        return ok(calendarService.getWeekly(CalendarScope.DOCTOR, doctorId, date, status, pageable));
    }

    @GetMapping("/doctors/{doctorId}/monthly")
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    @Operation(
            summary = "Doctor monthly calendar",
            description = "Per-day status counts only (use daily endpoint for event detail)."
    )
    public ResponseEntity<ApiResponse<CalendarMonthResponse>> doctorMonthly(
            @PathVariable("doctorId") final UUID doctorId,
            @RequestParam("year") final int year,
            @RequestParam("month") final int month,
            @RequestParam(value = "status", required = false) final AppointmentStatus status
    ) {
        return okMonth(calendarService.getMonthly(CalendarScope.DOCTOR, doctorId, year, month, status));
    }

    // --- Department ---

    @GetMapping("/departments/{departmentId}/daily")
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    @Operation(summary = "Department daily calendar")
    public ResponseEntity<ApiResponse<CalendarRangeResponse>> departmentDaily(
            @PathVariable("departmentId") final UUID departmentId,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date,
            @RequestParam(value = "status", required = false) final AppointmentStatus status,
            @PageableDefault(size = 50, sort = {"appointmentDate", "startTime"}, direction = Sort.Direction.ASC)
            final Pageable pageable
    ) {
        return ok(calendarService.getDaily(CalendarScope.DEPARTMENT, departmentId, date, status, pageable));
    }

    @GetMapping("/departments/{departmentId}/weekly")
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    @Operation(summary = "Department weekly calendar")
    public ResponseEntity<ApiResponse<CalendarRangeResponse>> departmentWeekly(
            @PathVariable("departmentId") final UUID departmentId,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date,
            @RequestParam(value = "status", required = false) final AppointmentStatus status,
            @PageableDefault(size = 100, sort = {"appointmentDate", "startTime"}, direction = Sort.Direction.ASC)
            final Pageable pageable
    ) {
        return ok(calendarService.getWeekly(CalendarScope.DEPARTMENT, departmentId, date, status, pageable));
    }

    @GetMapping("/departments/{departmentId}/monthly")
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    @Operation(summary = "Department monthly calendar")
    public ResponseEntity<ApiResponse<CalendarMonthResponse>> departmentMonthly(
            @PathVariable("departmentId") final UUID departmentId,
            @RequestParam("year") final int year,
            @RequestParam("month") final int month,
            @RequestParam(value = "status", required = false) final AppointmentStatus status
    ) {
        return okMonth(calendarService.getMonthly(CalendarScope.DEPARTMENT, departmentId, year, month, status));
    }

    // --- Hospital ---

    @GetMapping("/hospitals/{hospitalId}/daily")
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    @Operation(
            summary = "Hospital daily calendar",
            description = "Hospital-wide day view; pagination is required for large volumes (max page size 200)."
    )
    public ResponseEntity<ApiResponse<CalendarRangeResponse>> hospitalDaily(
            @PathVariable("hospitalId") final UUID hospitalId,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date,
            @RequestParam(value = "status", required = false) final AppointmentStatus status,
            @PageableDefault(size = 50, sort = {"appointmentDate", "startTime"}, direction = Sort.Direction.ASC)
            final Pageable pageable
    ) {
        return ok(calendarService.getDaily(CalendarScope.HOSPITAL, hospitalId, date, status, pageable));
    }

    @GetMapping("/hospitals/{hospitalId}/weekly")
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    @Operation(summary = "Hospital weekly calendar")
    public ResponseEntity<ApiResponse<CalendarRangeResponse>> hospitalWeekly(
            @PathVariable("hospitalId") final UUID hospitalId,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate date,
            @RequestParam(value = "status", required = false) final AppointmentStatus status,
            @PageableDefault(size = 100, sort = {"appointmentDate", "startTime"}, direction = Sort.Direction.ASC)
            final Pageable pageable
    ) {
        return ok(calendarService.getWeekly(CalendarScope.HOSPITAL, hospitalId, date, status, pageable));
    }

    @GetMapping("/hospitals/{hospitalId}/monthly")
    @RequirePermission(PermissionConstants.APPOINTMENT_READ)
    @Operation(summary = "Hospital monthly calendar")
    public ResponseEntity<ApiResponse<CalendarMonthResponse>> hospitalMonthly(
            @PathVariable("hospitalId") final UUID hospitalId,
            @RequestParam("year") final int year,
            @RequestParam("month") final int month,
            @RequestParam(value = "status", required = false) final AppointmentStatus status
    ) {
        return okMonth(calendarService.getMonthly(CalendarScope.HOSPITAL, hospitalId, year, month, status));
    }

    private static ResponseEntity<ApiResponse<CalendarRangeResponse>> ok(final CalendarRangeResponse body) {
        return ResponseEntity.ok(ApiResponse.success("Calendar retrieved successfully", body));
    }

    private static ResponseEntity<ApiResponse<CalendarMonthResponse>> okMonth(final CalendarMonthResponse body) {
        return ResponseEntity.ok(ApiResponse.success("Monthly calendar retrieved successfully", body));
    }
}
