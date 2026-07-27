package com.healthcare.hms.appointments.availability.support;

import com.healthcare.hms.appointments.support.AppointmentActorScopeSupport;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.organization.entity.Doctor;
import com.healthcare.hms.organization.repository.DoctorRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DoctorAvailabilityAccessSupport {

    private final DoctorRepository doctorRepository;
    private final AppointmentActorScopeSupport actorScopeSupport;

    public DoctorAvailabilityAccessSupport(
            final DoctorRepository doctorRepository,
            final AppointmentActorScopeSupport actorScopeSupport
    ) {
        this.doctorRepository = doctorRepository;
        this.actorScopeSupport = actorScopeSupport;
    }

    public Doctor requireDoctor(final UUID tenantId, final UUID doctorId) {
        actorScopeSupport.assertDoctorAccessible(tenantId, doctorId);
        return doctorRepository.findByIdAndTenantId(doctorId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
    }
}
