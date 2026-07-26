package com.healthcare.hms.patients.immunization.repository;

import com.healthcare.hms.patients.immunization.entity.Immunization;
import com.healthcare.hms.patients.immunization.enums.ImmunizationStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImmunizationRepository extends JpaRepository<Immunization, UUID> {

    Optional<Immunization> findByIdAndTenantIdAndPatientId(UUID id, UUID tenantId, UUID patientId);

    List<Immunization> findByTenantIdAndPatientIdOrderByAdministrationDateDescVaccineNameAscDoseNumberAsc(
            UUID tenantId,
            UUID patientId
    );

    List<Immunization> findByTenantIdAndPatientIdAndStatusOrderByAdministrationDateDescVaccineNameAscDoseNumberAsc(
            UUID tenantId,
            UUID patientId,
            ImmunizationStatus status
    );

    List<Immunization> findByTenantIdAndPatientIdAndStatusAndNextDueDateLessThanEqualOrderByNextDueDateAscVaccineNameAsc(
            UUID tenantId,
            UUID patientId,
            ImmunizationStatus status,
            LocalDate asOf
    );

    long countByTenantIdAndPatientIdAndStatusAndNextDueDateLessThanEqual(
            UUID tenantId,
            UUID patientId,
            ImmunizationStatus status,
            LocalDate asOf
    );
}
