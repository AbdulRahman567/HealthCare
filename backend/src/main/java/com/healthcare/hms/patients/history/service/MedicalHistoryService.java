package com.healthcare.hms.patients.history.service;

import com.healthcare.hms.patients.history.dto.request.UpsertChronicConditionRequest;
import com.healthcare.hms.patients.history.dto.request.UpsertFamilyHistoryRequest;
import com.healthcare.hms.patients.history.dto.request.UpsertPastDiseaseRequest;
import com.healthcare.hms.patients.history.dto.request.UpsertSurgeryHistoryRequest;
import com.healthcare.hms.patients.history.dto.response.ChronicConditionResponse;
import com.healthcare.hms.patients.history.dto.response.FamilyHistoryResponse;
import com.healthcare.hms.patients.history.dto.response.MedicalHistoryResponse;
import com.healthcare.hms.patients.history.dto.response.PastDiseaseResponse;
import com.healthcare.hms.patients.history.dto.response.SurgeryHistoryResponse;
import java.util.UUID;

/**
 * Structured medical history for a patient (Phase 5.3).
 *
 * <p>No visit linkage — clinical encounters are a later phase.
 */
public interface MedicalHistoryService {

    MedicalHistoryResponse getByPatientId(UUID patientId);

    PastDiseaseResponse addPastDisease(
            UUID patientId,
            UpsertPastDiseaseRequest request,
            String ipAddress,
            String userAgent
    );

    PastDiseaseResponse updatePastDisease(
            UUID patientId,
            UUID entryId,
            UpsertPastDiseaseRequest request,
            String ipAddress,
            String userAgent
    );

    void removePastDisease(UUID patientId, UUID entryId, String ipAddress, String userAgent);

    SurgeryHistoryResponse addSurgery(
            UUID patientId,
            UpsertSurgeryHistoryRequest request,
            String ipAddress,
            String userAgent
    );

    SurgeryHistoryResponse updateSurgery(
            UUID patientId,
            UUID entryId,
            UpsertSurgeryHistoryRequest request,
            String ipAddress,
            String userAgent
    );

    void removeSurgery(UUID patientId, UUID entryId, String ipAddress, String userAgent);

    ChronicConditionResponse addChronicCondition(
            UUID patientId,
            UpsertChronicConditionRequest request,
            String ipAddress,
            String userAgent
    );

    ChronicConditionResponse updateChronicCondition(
            UUID patientId,
            UUID entryId,
            UpsertChronicConditionRequest request,
            String ipAddress,
            String userAgent
    );

    void removeChronicCondition(UUID patientId, UUID entryId, String ipAddress, String userAgent);

    FamilyHistoryResponse addFamilyHistory(
            UUID patientId,
            UpsertFamilyHistoryRequest request,
            String ipAddress,
            String userAgent
    );

    FamilyHistoryResponse updateFamilyHistory(
            UUID patientId,
            UUID entryId,
            UpsertFamilyHistoryRequest request,
            String ipAddress,
            String userAgent
    );

    void removeFamilyHistory(UUID patientId, UUID entryId, String ipAddress, String userAgent);
}
