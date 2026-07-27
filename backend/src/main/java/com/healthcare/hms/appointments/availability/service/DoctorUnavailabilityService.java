package com.healthcare.hms.appointments.availability.service;

import com.healthcare.hms.appointments.availability.dto.request.UpsertDoctorUnavailabilityRequest;
import com.healthcare.hms.appointments.availability.dto.response.DoctorUnavailabilityResponse;
import com.healthcare.hms.appointments.availability.enums.UnavailabilityType;
import java.util.List;
import java.util.UUID;

public interface DoctorUnavailabilityService {

    DoctorUnavailabilityResponse create(
            UUID doctorId, UpsertDoctorUnavailabilityRequest request, String ipAddress, String userAgent);

    DoctorUnavailabilityResponse update(
            UUID doctorId,
            UUID unavailabilityId,
            UpsertDoctorUnavailabilityRequest request,
            String ipAddress,
            String userAgent);

    DoctorUnavailabilityResponse getById(UUID doctorId, UUID unavailabilityId);

    List<DoctorUnavailabilityResponse> list(UUID doctorId, UnavailabilityType type);

    void delete(UUID doctorId, UUID unavailabilityId, String ipAddress, String userAgent);
}
