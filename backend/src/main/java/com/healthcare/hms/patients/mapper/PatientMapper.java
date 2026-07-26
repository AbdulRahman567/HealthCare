package com.healthcare.hms.patients.mapper;

import com.healthcare.hms.patients.dto.request.EmergencyContactRequest;
import com.healthcare.hms.patients.dto.request.RegisterPatientRequest;
import com.healthcare.hms.patients.dto.request.UpdatePatientRequest;
import com.healthcare.hms.patients.dto.response.EmergencyContactResponse;
import com.healthcare.hms.patients.dto.response.PatientResponse;
import com.healthcare.hms.patients.entity.EmergencyContact;
import com.healthcare.hms.patients.entity.Patient;
import com.healthcare.hms.patients.enums.BloodGroup;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

/**
 * Maps patient persistence models to API DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PatientMapper {

    @Mapping(target = "emergencyContact", source = "emergencyContact")
    PatientResponse toResponse(Patient patient);

    EmergencyContactResponse toEmergencyContactResponse(EmergencyContact contact);

    @Mapping(target = "mrn", expression = "java(normalizeMrn(request.mrn()))")
    @Mapping(target = "firstName", expression = "java(request.firstName().trim())")
    @Mapping(target = "lastName", expression = "java(request.lastName().trim())")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "gender", source = "gender")
    @Mapping(target = "bloodGroup", expression = "java(defaultBloodGroup(request.bloodGroup()))")
    @Mapping(target = "nationalId", source = "nationalId", qualifiedByName = "trimToNull")
    @Mapping(target = "phone", source = "phone", qualifiedByName = "trimToNull")
    @Mapping(target = "email", source = "email", qualifiedByName = "normalizeEmail")
    @Mapping(target = "address", source = "address", qualifiedByName = "trimToNull")
    @Mapping(target = "maritalStatus", source = "maritalStatus")
    @Mapping(target = "emergencyContact", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "primaryDepartmentId", ignore = true)
    @Mapping(target = "primaryDoctorId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void applyRegister(RegisterPatientRequest request, @MappingTarget Patient patient);

    @Mapping(target = "mrn", expression = "java(normalizeMrn(request.mrn()))")
    @Mapping(target = "firstName", expression = "java(request.firstName().trim())")
    @Mapping(target = "lastName", expression = "java(request.lastName().trim())")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "gender", source = "gender")
    @Mapping(target = "bloodGroup", expression = "java(defaultBloodGroup(request.bloodGroup()))")
    @Mapping(target = "nationalId", source = "nationalId", qualifiedByName = "trimToNull")
    @Mapping(target = "phone", source = "phone", qualifiedByName = "trimToNull")
    @Mapping(target = "email", source = "email", qualifiedByName = "normalizeEmail")
    @Mapping(target = "address", source = "address", qualifiedByName = "trimToNull")
    @Mapping(target = "maritalStatus", source = "maritalStatus")
    @Mapping(target = "emergencyContact", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "primaryDepartmentId", ignore = true)
    @Mapping(target = "primaryDoctorId", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenantId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void applyUpdate(UpdatePatientRequest request, @MappingTarget Patient patient);

    @AfterMapping
    default void mapEmergencyContactOnRegister(
            final RegisterPatientRequest request,
            @MappingTarget final Patient patient
    ) {
        patient.setEmergencyContact(toEmergencyContact(request.emergencyContact()));
    }

    @AfterMapping
    default void mapEmergencyContactOnUpdate(
            final UpdatePatientRequest request,
            @MappingTarget final Patient patient
    ) {
        patient.setEmergencyContact(toEmergencyContact(request.emergencyContact()));
    }

    default EmergencyContact toEmergencyContact(final EmergencyContactRequest request) {
        final EmergencyContact contact = new EmergencyContact();
        if (request == null) {
            return contact;
        }
        contact.setName(trimToNull(request.name()));
        contact.setPhone(trimToNull(request.phone()));
        contact.setRelation(trimToNull(request.relation()));
        return contact;
    }

    default String normalizeMrn(final String mrn) {
        return mrn.trim().toUpperCase(java.util.Locale.ROOT);
    }

    default BloodGroup defaultBloodGroup(final BloodGroup bloodGroup) {
        return bloodGroup != null ? bloodGroup : BloodGroup.UNKNOWN;
    }

    @Named("trimToNull")
    default String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Named("normalizeEmail")
    default String normalizeEmail(final String email) {
        final String trimmed = trimToNull(email);
        return trimmed == null ? null : trimmed.toLowerCase(java.util.Locale.ROOT);
    }
}
