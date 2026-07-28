package com.healthcare.hms.prescriptions.controller;

import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.prescriptions.dto.response.PrescriptionResponse;
import com.healthcare.hms.prescriptions.service.PrescriptionService;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.users.constant.PermissionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Patient prescription history (cross-consultation).
 */
@RestController
@RequestMapping("/api/v1/patients/{patientId}/prescriptions")
@Validated
@Tag(
        name = "Patient Prescription History",
        description = "Paginated prescriptions across consultations for longitudinal medication review"
)
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class PatientPrescriptionHistoryController {

    private final PrescriptionService prescriptionService;

    public PatientPrescriptionHistoryController(final PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @GetMapping
    @RequirePermission(PermissionConstants.PRESCRIPTION_READ)
    @Operation(
            summary = "Patient prescription history",
            description = """
                    Paginated list of prescriptions for the patient, newest prescriptionDate first.
                    Cross-doctor visibility scoped by patient for complete medication history.
                    """
    )
    public ResponseEntity<ApiResponse<PageResponse<PrescriptionResponse>>> history(
            @PathVariable final UUID patientId,
            @PageableDefault(size = 20, sort = "prescriptionDate", direction = Sort.Direction.DESC)
            final Pageable pageable
    ) {
        final PageResponse<PrescriptionResponse> page = prescriptionService.patientHistory(patientId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Patient prescription history retrieved", page));
    }
}
