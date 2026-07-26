package com.healthcare.hms.patients.history.controller;

import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.web.ClientRequestDetails;
import com.healthcare.hms.patients.history.dto.request.UpsertChronicConditionRequest;
import com.healthcare.hms.patients.history.dto.request.UpsertPastDiseaseRequest;
import com.healthcare.hms.patients.history.dto.request.UpsertSurgeryHistoryRequest;
import com.healthcare.hms.patients.history.dto.response.ChronicConditionResponse;
import com.healthcare.hms.patients.history.dto.response.MedicalHistoryResponse;
import com.healthcare.hms.patients.history.dto.response.PastDiseaseResponse;
import com.healthcare.hms.patients.history.dto.response.SurgeryHistoryResponse;
import com.healthcare.hms.patients.history.service.MedicalHistoryService;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.users.constant.PermissionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Structured patient medical history API (Phase 5.3).
 *
 * <p>No visit endpoints — clinical encounters are out of scope.
 */
@RestController
@RequestMapping("/api/v1/patients/{patientId}/medical-history")
@Tag(name = "Medical History", description = "Structured past diseases, surgeries, and chronic conditions")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class MedicalHistoryController {

    private final MedicalHistoryService medicalHistoryService;

    public MedicalHistoryController(final MedicalHistoryService medicalHistoryService) {
        this.medicalHistoryService = medicalHistoryService;
    }

    @GetMapping
    @RequirePermission(PermissionConstants.PATIENT_READ)
    @Operation(summary = "Get patient medical history", description = "Returns structured history entries. Empty collections when none recorded.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Medical history retrieved",
                    content = @Content(schema = @Schema(implementation = MedicalHistoryResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient not found")
    })
    public ResponseEntity<ApiResponse<MedicalHistoryResponse>> get(
            @PathVariable("patientId") final UUID patientId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Medical history retrieved successfully",
                medicalHistoryService.getByPatientId(patientId)
        ));
    }

    @PostMapping("/past-diseases")
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    @Operation(summary = "Add past disease")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient not found")
    })
    public ResponseEntity<ApiResponse<PastDiseaseResponse>> addPastDisease(
            @PathVariable("patientId") final UUID patientId,
            @Valid @RequestBody final UpsertPastDiseaseRequest request,
            final HttpServletRequest httpRequest
    ) {
        final PastDiseaseResponse response = medicalHistoryService.addPastDisease(
                patientId,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Past disease added successfully", response));
    }

    @PutMapping("/past-diseases/{entryId}")
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    @Operation(summary = "Update past disease")
    public ResponseEntity<ApiResponse<PastDiseaseResponse>> updatePastDisease(
            @PathVariable("patientId") final UUID patientId,
            @PathVariable("entryId") final UUID entryId,
            @Valid @RequestBody final UpsertPastDiseaseRequest request,
            final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Past disease updated successfully",
                medicalHistoryService.updatePastDisease(
                        patientId,
                        entryId,
                        request,
                        ClientRequestDetails.resolveClientIp(httpRequest),
                        ClientRequestDetails.resolveUserAgent(httpRequest)
                )
        ));
    }

    @DeleteMapping("/past-diseases/{entryId}")
    @RequirePermission(PermissionConstants.PATIENT_DELETE)
    @Operation(summary = "Soft-delete past disease", description = "Logical removal only — chart data is retained for audit.")
    public ResponseEntity<Void> removePastDisease(
            @PathVariable("patientId") final UUID patientId,
            @PathVariable("entryId") final UUID entryId,
            final HttpServletRequest httpRequest
    ) {
        medicalHistoryService.removePastDisease(
                patientId,
                entryId,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/surgeries")
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    @Operation(summary = "Add surgery history", description = "diagnosisDate is the procedure date.")
    public ResponseEntity<ApiResponse<SurgeryHistoryResponse>> addSurgery(
            @PathVariable("patientId") final UUID patientId,
            @Valid @RequestBody final UpsertSurgeryHistoryRequest request,
            final HttpServletRequest httpRequest
    ) {
        final SurgeryHistoryResponse response = medicalHistoryService.addSurgery(
                patientId,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Surgery history added successfully", response));
    }

    @PutMapping("/surgeries/{entryId}")
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    @Operation(summary = "Update surgery history")
    public ResponseEntity<ApiResponse<SurgeryHistoryResponse>> updateSurgery(
            @PathVariable("patientId") final UUID patientId,
            @PathVariable("entryId") final UUID entryId,
            @Valid @RequestBody final UpsertSurgeryHistoryRequest request,
            final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Surgery history updated successfully",
                medicalHistoryService.updateSurgery(
                        patientId,
                        entryId,
                        request,
                        ClientRequestDetails.resolveClientIp(httpRequest),
                        ClientRequestDetails.resolveUserAgent(httpRequest)
                )
        ));
    }

    @DeleteMapping("/surgeries/{entryId}")
    @RequirePermission(PermissionConstants.PATIENT_DELETE)
    @Operation(summary = "Soft-delete surgery history")
    public ResponseEntity<Void> removeSurgery(
            @PathVariable("patientId") final UUID patientId,
            @PathVariable("entryId") final UUID entryId,
            final HttpServletRequest httpRequest
    ) {
        medicalHistoryService.removeSurgery(
                patientId,
                entryId,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/chronic-conditions")
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    @Operation(summary = "Add chronic condition")
    public ResponseEntity<ApiResponse<ChronicConditionResponse>> addChronicCondition(
            @PathVariable("patientId") final UUID patientId,
            @Valid @RequestBody final UpsertChronicConditionRequest request,
            final HttpServletRequest httpRequest
    ) {
        final ChronicConditionResponse response = medicalHistoryService.addChronicCondition(
                patientId,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Chronic condition added successfully", response));
    }

    @PutMapping("/chronic-conditions/{entryId}")
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    @Operation(summary = "Update chronic condition")
    public ResponseEntity<ApiResponse<ChronicConditionResponse>> updateChronicCondition(
            @PathVariable("patientId") final UUID patientId,
            @PathVariable("entryId") final UUID entryId,
            @Valid @RequestBody final UpsertChronicConditionRequest request,
            final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Chronic condition updated successfully",
                medicalHistoryService.updateChronicCondition(
                        patientId,
                        entryId,
                        request,
                        ClientRequestDetails.resolveClientIp(httpRequest),
                        ClientRequestDetails.resolveUserAgent(httpRequest)
                )
        ));
    }

    @DeleteMapping("/chronic-conditions/{entryId}")
    @RequirePermission(PermissionConstants.PATIENT_DELETE)
    @Operation(summary = "Soft-delete chronic condition")
    public ResponseEntity<Void> removeChronicCondition(
            @PathVariable("patientId") final UUID patientId,
            @PathVariable("entryId") final UUID entryId,
            final HttpServletRequest httpRequest
    ) {
        medicalHistoryService.removeChronicCondition(
                patientId,
                entryId,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.noContent().build();
    }
}
