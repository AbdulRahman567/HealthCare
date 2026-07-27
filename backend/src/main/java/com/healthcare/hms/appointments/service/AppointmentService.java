package com.healthcare.hms.appointments.service;

import com.healthcare.hms.appointments.dto.request.AppointmentSearchCriteria;
import com.healthcare.hms.appointments.dto.request.CancelAppointmentRequest;
import com.healthcare.hms.appointments.dto.request.CreateAppointmentRequest;
import com.healthcare.hms.appointments.dto.request.RescheduleAppointmentRequest;
import com.healthcare.hms.appointments.dto.request.UpdateAppointmentRequest;
import com.healthcare.hms.appointments.dto.response.AppointmentResponse;
import com.healthcare.hms.common.api.PageResponse;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface AppointmentService {

    AppointmentResponse create(CreateAppointmentRequest request, String ipAddress, String userAgent);

    AppointmentResponse update(UUID appointmentId, UpdateAppointmentRequest request, String ipAddress, String userAgent);

    AppointmentResponse reschedule(
            UUID appointmentId, RescheduleAppointmentRequest request, String ipAddress, String userAgent);

    AppointmentResponse cancel(
            UUID appointmentId, CancelAppointmentRequest request, String ipAddress, String userAgent);

    AppointmentResponse confirm(UUID appointmentId, String ipAddress, String userAgent);

    AppointmentResponse getById(UUID appointmentId, String ipAddress, String userAgent);

    /**
     * Tenant-scoped appointment directory search with DB Specifications, pagination, and sorting.
     */
    PageResponse<AppointmentResponse> search(AppointmentSearchCriteria criteria, Pageable pageable);
}
