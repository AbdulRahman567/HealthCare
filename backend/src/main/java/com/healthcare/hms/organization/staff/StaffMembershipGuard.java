package com.healthcare.hms.organization.staff;

import com.healthcare.hms.organization.repository.DoctorRepository;
import com.healthcare.hms.organization.repository.LaboratoryStaffRepository;
import com.healthcare.hms.organization.repository.NurseRepository;
import com.healthcare.hms.organization.repository.PharmacistRepository;
import com.healthcare.hms.organization.repository.ReceptionistRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Ensures a user holds at most one staff employment profile across types,
 * and detects department affiliations before department soft-delete.
 */
@Component
public class StaffMembershipGuard {

    private final DoctorRepository doctorRepository;
    private final NurseRepository nurseRepository;
    private final ReceptionistRepository receptionistRepository;
    private final LaboratoryStaffRepository laboratoryStaffRepository;
    private final PharmacistRepository pharmacistRepository;

    public StaffMembershipGuard(
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

    public boolean hasAffiliatedStaff(final UUID departmentId) {
        return doctorRepository.existsByDepartmentId(departmentId)
                || nurseRepository.existsByDepartmentId(departmentId)
                || receptionistRepository.existsByDepartmentId(departmentId)
                || laboratoryStaffRepository.existsByDepartmentId(departmentId)
                || pharmacistRepository.existsByDepartmentId(departmentId);
    }

    public boolean isEmployedElsewhereExcludingDoctor(final UUID userId) {
        return nurseRepository.existsByUserId(userId)
                || receptionistRepository.existsByUserId(userId)
                || laboratoryStaffRepository.existsByUserId(userId)
                || pharmacistRepository.existsByUserId(userId);
    }

    public boolean isEmployedElsewhereExcludingNurse(final UUID userId) {
        return doctorRepository.existsByUserId(userId)
                || receptionistRepository.existsByUserId(userId)
                || laboratoryStaffRepository.existsByUserId(userId)
                || pharmacistRepository.existsByUserId(userId);
    }

    public boolean isEmployedElsewhereExcludingReceptionist(final UUID userId) {
        return doctorRepository.existsByUserId(userId)
                || nurseRepository.existsByUserId(userId)
                || laboratoryStaffRepository.existsByUserId(userId)
                || pharmacistRepository.existsByUserId(userId);
    }

    public boolean isEmployedElsewhereExcludingLaboratoryStaff(final UUID userId) {
        return doctorRepository.existsByUserId(userId)
                || nurseRepository.existsByUserId(userId)
                || receptionistRepository.existsByUserId(userId)
                || pharmacistRepository.existsByUserId(userId);
    }

    public boolean isEmployedElsewhereExcludingPharmacist(final UUID userId) {
        return doctorRepository.existsByUserId(userId)
                || nurseRepository.existsByUserId(userId)
                || receptionistRepository.existsByUserId(userId)
                || laboratoryStaffRepository.existsByUserId(userId);
    }
}
