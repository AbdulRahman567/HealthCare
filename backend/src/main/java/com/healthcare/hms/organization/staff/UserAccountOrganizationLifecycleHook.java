package com.healthcare.hms.organization.staff;

import com.healthcare.hms.organization.entity.Department;
import com.healthcare.hms.organization.entity.Doctor;
import com.healthcare.hms.organization.entity.LaboratoryStaff;
import com.healthcare.hms.organization.entity.Nurse;
import com.healthcare.hms.organization.entity.Pharmacist;
import com.healthcare.hms.organization.entity.Receptionist;
import com.healthcare.hms.organization.entity.Staff;
import com.healthcare.hms.organization.enums.EmploymentStatus;
import com.healthcare.hms.organization.repository.DepartmentRepository;
import com.healthcare.hms.organization.repository.DoctorRepository;
import com.healthcare.hms.organization.repository.LaboratoryStaffRepository;
import com.healthcare.hms.organization.repository.NurseRepository;
import com.healthcare.hms.organization.repository.PharmacistRepository;
import com.healthcare.hms.organization.repository.ReceptionistRepository;
import com.healthcare.hms.users.service.UserAccountLifecycleHook;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Keeps org data consistent when a user account is deactivated or suspended.
 */
@Component
public class UserAccountOrganizationLifecycleHook implements UserAccountLifecycleHook {

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final NurseRepository nurseRepository;
    private final ReceptionistRepository receptionistRepository;
    private final LaboratoryStaffRepository laboratoryStaffRepository;
    private final PharmacistRepository pharmacistRepository;

    public UserAccountOrganizationLifecycleHook(
            final DepartmentRepository departmentRepository,
            final DoctorRepository doctorRepository,
            final NurseRepository nurseRepository,
            final ReceptionistRepository receptionistRepository,
            final LaboratoryStaffRepository laboratoryStaffRepository,
            final PharmacistRepository pharmacistRepository
    ) {
        this.departmentRepository = departmentRepository;
        this.doctorRepository = doctorRepository;
        this.nurseRepository = nurseRepository;
        this.receptionistRepository = receptionistRepository;
        this.laboratoryStaffRepository = laboratoryStaffRepository;
        this.pharmacistRepository = pharmacistRepository;
    }

    @Override
    @Transactional
    public void onUserAuthenticationDisabled(
            final UUID tenantId,
            final UUID userId,
            final UUID actorId
    ) {
        clearDepartmentHeads(tenantId, userId);
        suspendEmploymentIfActive(doctorRepository.findByTenantIdAndUserId(tenantId, userId), actorId);
        suspendEmploymentIfActive(nurseRepository.findByTenantIdAndUserId(tenantId, userId), actorId);
        suspendEmploymentIfActive(receptionistRepository.findByTenantIdAndUserId(tenantId, userId), actorId);
        suspendEmploymentIfActive(laboratoryStaffRepository.findByTenantIdAndUserId(tenantId, userId), actorId);
        suspendEmploymentIfActive(pharmacistRepository.findByTenantIdAndUserId(tenantId, userId), actorId);
    }

    private void clearDepartmentHeads(final UUID tenantId, final UUID userId) {
        final List<Department> headed = departmentRepository.findByTenantIdAndHeadUserId(tenantId, userId);
        for (final Department department : headed) {
            department.clearHead();
            departmentRepository.save(department);
        }
    }

    private <T extends Staff> void suspendEmploymentIfActive(
            final Optional<T> staffOpt,
            final UUID actorId
    ) {
        staffOpt.ifPresent(staff -> {
            if (staff.getEmploymentStatus() == EmploymentStatus.ACTIVE
                    || staff.getEmploymentStatus() == EmploymentStatus.ON_LEAVE
                    || staff.getEmploymentStatus() == EmploymentStatus.PENDING) {
                staff.setEmploymentStatus(EmploymentStatus.SUSPENDED);
                staff.setUpdatedBy(actorId);
                saveStaff(staff);
            }
        });
    }

    private void saveStaff(final Staff staff) {
        if (staff instanceof Doctor doctor) {
            doctorRepository.save(doctor);
        } else if (staff instanceof Nurse nurse) {
            nurseRepository.save(nurse);
        } else if (staff instanceof Receptionist receptionist) {
            receptionistRepository.save(receptionist);
        } else if (staff instanceof LaboratoryStaff laboratoryStaff) {
            laboratoryStaffRepository.save(laboratoryStaff);
        } else if (staff instanceof Pharmacist pharmacist) {
            pharmacistRepository.save(pharmacist);
        }
    }
}
