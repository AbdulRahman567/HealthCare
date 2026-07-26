package com.healthcare.hms.patients.allergy.repository;

import com.healthcare.hms.patients.allergy.entity.Allergy;
import com.healthcare.hms.patients.allergy.enums.AllergyStatus;
import com.healthcare.hms.patients.allergy.enums.AllergyType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AllergyRepository extends JpaRepository<Allergy, UUID> {

    Optional<Allergy> findByIdAndTenantIdAndPatientId(UUID id, UUID tenantId, UUID patientId);

    List<Allergy> findByTenantIdAndPatientIdOrderBySeverityDescAllergenNameAsc(UUID tenantId, UUID patientId);

    List<Allergy> findByTenantIdAndPatientIdAndAllergyTypeOrderBySeverityDescAllergenNameAsc(
            UUID tenantId,
            UUID patientId,
            AllergyType allergyType
    );

    List<Allergy> findByTenantIdAndPatientIdAndStatusAndShowOnBannerTrueOrderBySeverityDescAllergenNameAsc(
            UUID tenantId,
            UUID patientId,
            AllergyStatus status
    );

    List<Allergy> findByTenantIdAndPatientIdAndStatusAndCriticalAlertTrueOrderBySeverityDescAllergenNameAsc(
            UUID tenantId,
            UUID patientId,
            AllergyStatus status
    );

    boolean existsByTenantIdAndPatientIdAndStatusAndAllergyType(
            UUID tenantId,
            UUID patientId,
            AllergyStatus status,
            AllergyType allergyType
    );

    long countByTenantIdAndPatientIdAndStatusAndCriticalAlertTrue(
            UUID tenantId,
            UUID patientId,
            AllergyStatus status
    );
}
