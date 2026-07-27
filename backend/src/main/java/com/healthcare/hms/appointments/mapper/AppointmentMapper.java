package com.healthcare.hms.appointments.mapper;

import com.healthcare.hms.appointments.dto.request.CreateAppointmentRequest;
import com.healthcare.hms.appointments.dto.request.RescheduleAppointmentRequest;
import com.healthcare.hms.appointments.dto.request.UpdateAppointmentRequest;
import com.healthcare.hms.appointments.dto.response.AppointmentResponse;
import com.healthcare.hms.appointments.entity.Appointment;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public void applyCreate(final CreateAppointmentRequest request, final Appointment appointment) {
        appointment.setPatientId(request.patientId());
        appointment.setDoctorId(request.doctorId());
        appointment.setDepartmentId(request.departmentId());
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(request.endTime());
        appointment.setDurationMinutes(request.durationMinutes());
        appointment.setAppointmentType(request.appointmentType());
        appointment.setVisitType(request.visitType());
        appointment.setNotes(trimToNull(request.notes()));
    }

    public void applyUpdate(final UpdateAppointmentRequest request, final Appointment appointment) {
        appointment.setDoctorId(request.doctorId());
        appointment.setDepartmentId(request.departmentId());
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(request.endTime());
        appointment.setDurationMinutes(request.durationMinutes());
        appointment.setAppointmentType(request.appointmentType());
        appointment.setVisitType(request.visitType());
        appointment.setNotes(trimToNull(request.notes()));
    }

    public void applyReschedule(final RescheduleAppointmentRequest request, final Appointment appointment) {
        appointment.setDoctorId(request.doctorId());
        appointment.setDepartmentId(request.departmentId());
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setStartTime(request.startTime());
        appointment.setEndTime(request.endTime());
        appointment.setDurationMinutes(request.durationMinutes());
        appointment.clearConfirmationAfterReschedule();
    }

    public AppointmentResponse toResponse(final Appointment appointment) {
        return toResponse(appointment, null, null);
    }

    public AppointmentResponse toResponse(
            final Appointment appointment,
            final String patientName,
            final String patientMrn
    ) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getAppointmentNumber(),
                appointment.getHospitalId(),
                appointment.getPatientId(),
                patientName,
                patientMrn,
                appointment.getDoctorId(),
                appointment.getDepartmentId(),
                appointment.getAppointmentDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getDurationMinutes(),
                appointment.getStatus(),
                appointment.getAppointmentType(),
                appointment.getVisitType(),
                appointment.getNotes(),
                appointment.getConfirmedAt(),
                appointment.getCancelledAt(),
                appointment.getCancellationReason(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt(),
                appointment.getVersion()
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
