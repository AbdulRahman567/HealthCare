package com.healthcare.hms.prescriptions.repository;

import com.healthcare.hms.prescriptions.entity.PrescriptionItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, UUID> {

    Optional<PrescriptionItem> findByIdAndTenantIdAndPrescriptionId(UUID id, UUID tenantId, UUID prescriptionId);

    List<PrescriptionItem> findByTenantIdAndPrescriptionIdOrderBySequenceNumberAsc(
            UUID tenantId, UUID prescriptionId);

    boolean existsByTenantIdAndPrescriptionIdAndMedicineNameKey(
            UUID tenantId, UUID prescriptionId, String medicineNameKey);

    boolean existsByTenantIdAndPrescriptionIdAndMedicineNameKeyAndIdNot(
            UUID tenantId, UUID prescriptionId, String medicineNameKey, UUID id);

    long countByTenantIdAndPrescriptionId(UUID tenantId, UUID prescriptionId);
}
