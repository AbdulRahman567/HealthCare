package com.healthcare.hms.clinical.vitals.controller;

import com.healthcare.hms.clinical.vitals.dto.response.VitalSignsResponse;
import com.healthcare.hms.clinical.vitals.service.VitalSignsService;
import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.users.constant.PermissionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Patient vital-signs history (cross-consultation time series).
 *
 * <p>Supports trend analysis across all encounters — not scoped to a single doctor
 * (healthcare-domain: complete patient story).
 */
@RestController
@RequestMapping("/api/v1/patients/{patientId}/vital-signs")
@Validated
@Tag(name = "Patient Vital Signs History", description = "Paginated vital-signs time series for trend analysis")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class PatientVitalSignsHistoryController {

    private final VitalSignsService vitalSignsService;

    public PatientVitalSignsHistoryController(final VitalSignsService vitalSignsService) {
        this.vitalSignsService = vitalSignsService;
    }

    @GetMapping
    @RequirePermission(PermissionConstants.VISIT_READ)
    @Operation(
            summary = "Patient vital signs history",
            description = """
                    Paginated time series of every vital-signs measurement for the patient,
                    newest first. Optional inclusive date range on recordedAt. Cross-doctor
                    visibility for longitudinal trend review.
                    """
    )
    public ResponseEntity<ApiResponse<PageResponse<VitalSignsResponse>>> history(
            @PathVariable final UUID patientId,
            @Parameter(description = "Inclusive start date (recordedAt)")
            @RequestParam(value = "fromDate", required = false) final LocalDate fromDate,
            @Parameter(description = "Inclusive end date (recordedAt)")
            @RequestParam(value = "toDate", required = false) final LocalDate toDate,
            @PageableDefault(size = 20, sort = "recordedAt", direction = Sort.Direction.DESC)
            final Pageable pageable
    ) {
        final PageResponse<VitalSignsResponse> page = vitalSignsService.patientHistory(
                patientId,
                fromDate,
                toDate,
                pageable
        );
        return ResponseEntity.ok(ApiResponse.success("Patient vital signs history retrieved", page));
    }
}
