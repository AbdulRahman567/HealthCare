package com.healthcare.hms.appointments.availability.service;

import com.healthcare.hms.appointments.availability.dto.request.UpsertDoctorScheduleRequest;
import com.healthcare.hms.appointments.availability.dto.response.DoctorScheduleResponse;
import java.util.List;
import java.util.UUID;

public interface DoctorScheduleService {

    DoctorScheduleResponse create(UUID doctorId, UpsertDoctorScheduleRequest request, String ipAddress, String userAgent);

    DoctorScheduleResponse update(
            UUID doctorId, UUID scheduleId, UpsertDoctorScheduleRequest request, String ipAddress, String userAgent);

    DoctorScheduleResponse getById(UUID doctorId, UUID scheduleId);

    List<DoctorScheduleResponse> list(UUID doctorId);

    void delete(UUID doctorId, UUID scheduleId, String ipAddress, String userAgent);
}
