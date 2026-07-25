package com.healthcare.hms.organization.staff;

import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.organization.entity.Doctor;
import com.healthcare.hms.organization.entity.LaboratoryStaff;
import com.healthcare.hms.organization.entity.Nurse;
import com.healthcare.hms.organization.entity.Pharmacist;
import com.healthcare.hms.organization.entity.Receptionist;
import com.healthcare.hms.organization.entity.Staff;
import com.healthcare.hms.organization.enums.StaffType;
import com.healthcare.hms.organization.repository.DoctorRepository;
import com.healthcare.hms.organization.repository.LaboratoryStaffRepository;
import com.healthcare.hms.organization.repository.NurseRepository;
import com.healthcare.hms.organization.repository.PharmacistRepository;
import com.healthcare.hms.organization.repository.ReceptionistRepository;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import org.springframework.stereotype.Component;

/**
 * Resolves polymorphic staff rows across specialization tables (Phase 4.4).
 */
@Component
public class StaffProfileDirectory {

    private final DoctorRepository doctorRepository;
    private final NurseRepository nurseRepository;
    private final ReceptionistRepository receptionistRepository;
    private final LaboratoryStaffRepository laboratoryStaffRepository;
    private final PharmacistRepository pharmacistRepository;

    public StaffProfileDirectory(
            final DoctorRepository doctorRepository,
            final NurseRepository nurseRepository,
            final ReceptionistRepository receptionistRepository,
            final LaboratoryStaffRepository laboratoryStaffRepository,
            final PharmacistRepository pharmacistRepository
    ) {
        this.doctorRepository = doctorRepository;
        this.nurseRepository = nurseRepository;
        this.receptionistRepository = receptionistRepository;
        this.laboratoryStaffRepository = laboratoryStaffRepository;
        this.pharmacistRepository = pharmacistRepository;
    }

    public Staff require(final StaffType staffType, final UUID staffId) {
        Objects.requireNonNull(staffType, "staffType");
        Objects.requireNonNull(staffId, "staffId");
        final UUID tenantId = TenantContextHolder.requireTenantId();
        return switch (staffType) {
            case DOCTOR -> requireRow(doctorRepository::findByIdAndTenantId, staffId, tenantId, "Doctor");
            case NURSE -> requireRow(nurseRepository::findByIdAndTenantId, staffId, tenantId, "Nurse");
            case RECEPTIONIST -> requireRow(
                    receptionistRepository::findByIdAndTenantId, staffId, tenantId, "Receptionist");
            case LABORATORY_STAFF -> requireRow(
                    laboratoryStaffRepository::findByIdAndTenantId, staffId, tenantId, "Laboratory staff");
            case PHARMACIST -> requireRow(
                    pharmacistRepository::findByIdAndTenantId, staffId, tenantId, "Pharmacist");
        };
    }

    public Staff save(final StaffType staffType, final Staff staff) {
        return switch (staffType) {
            case DOCTOR -> doctorRepository.save((Doctor) staff);
            case NURSE -> nurseRepository.save((Nurse) staff);
            case RECEPTIONIST -> receptionistRepository.save((Receptionist) staff);
            case LABORATORY_STAFF -> laboratoryStaffRepository.save((LaboratoryStaff) staff);
            case PHARMACIST -> pharmacistRepository.save((Pharmacist) staff);
        };
    }

    private static <T extends Staff> T requireRow(
            final BiFunction<UUID, UUID, java.util.Optional<T>> finder,
            final UUID staffId,
            final UUID tenantId,
            final String label
    ) {
        return finder.apply(staffId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(label + " not found"));
    }
}
