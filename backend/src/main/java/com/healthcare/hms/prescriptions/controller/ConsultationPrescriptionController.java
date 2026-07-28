package com.healthcare.hms.prescriptions.controller;

import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.prescriptions.dto.response.PrescriptionResponse;
import com.healthcare.hms.prescriptions.service.PrescriptionService;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.users.constant.PermissionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consultation-scoped prescription listing (Phase 7.5).
 */
@RestController
@RequestMapping("/api/v1/consultations/{consultationId}/prescriptions")
@Validated
@Tag(name = "Consultation Prescriptions", description = "Prescriptions authored during a consultation")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class ConsultationPrescriptionController {

    private final PrescriptionService prescriptionService;

    public ConsultationPrescriptionController(final PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @GetMapping
    @RequirePermission(PermissionConstants.PRESCRIPTION_READ)
    @Operation(
            summary = "List consultation prescriptions",
            description = "Returns all prescriptions for the consultation, newest first."
    )
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> list(
            @PathVariable final UUID consultationId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Consultation prescriptions retrieved successfully",
                prescriptionService.listByConsultation(consultationId)
        ));
    }
}
