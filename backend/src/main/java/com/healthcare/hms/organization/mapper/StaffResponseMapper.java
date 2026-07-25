package com.healthcare.hms.organization.mapper;

import com.healthcare.hms.organization.dto.response.DoctorResponse;
import com.healthcare.hms.organization.dto.response.LaboratoryStaffResponse;
import com.healthcare.hms.organization.dto.response.NurseResponse;
import com.healthcare.hms.organization.dto.response.PharmacistResponse;
import com.healthcare.hms.organization.dto.response.ReceptionistResponse;
import com.healthcare.hms.organization.entity.Doctor;
import com.healthcare.hms.organization.entity.LaboratoryStaff;
import com.healthcare.hms.organization.entity.Nurse;
import com.healthcare.hms.organization.entity.Pharmacist;
import com.healthcare.hms.organization.entity.Receptionist;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StaffResponseMapper {

    DoctorResponse toDoctorResponse(Doctor doctor);

    NurseResponse toNurseResponse(Nurse nurse);

    ReceptionistResponse toReceptionistResponse(Receptionist receptionist);

    LaboratoryStaffResponse toLaboratoryStaffResponse(LaboratoryStaff staff);

    PharmacistResponse toPharmacistResponse(Pharmacist pharmacist);
}
