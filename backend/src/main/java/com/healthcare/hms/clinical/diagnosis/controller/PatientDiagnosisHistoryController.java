package com.healthcare.hms.clinical.diagnosis.controller;

import com.healthcare.hms.clinical.diagnosis.dto.response.DiagnosisResponse;
import com.healthcare.hms.clinical.diagnosis.service.DiagnosisService;
import com.healthcare.hms.clinical.enums.DiagnosisStatus;
import com.healthcare.hms.clinical.enums.DiagnosisType;
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
 * Patient diagnosis history (cross-consultation encounter assertions).
 */
@RestController
@RequestMapping("/api/v1/patients/{patientId}/diagnoses")
@Validated
@Tag(
        name = "Patient Diagnosis History",
        description = "Paginated encounter diagnoses across consultations for longitudinal review"
)
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class PatientDiagnosisHistoryController {

    private final DiagnosisService diagnosisService;

    public PatientDiagnosisHistoryController(final DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    @GetMapping
    @RequirePermission(PermissionConstants.VISIT_READ)
    @Operation(
            summary = "Patient diagnosis history",
            description = """
                    Paginated list of encounter diagnoses for the patient, newest diagnosedAt first.
                    Optional filters by type, status, and diagnosedAt date range. Cross-doctor
                    visibility scoped by patient (not current doctor).
                    """
    )
    public ResponseEntity<ApiResponse<PageResponse<DiagnosisResponse>>> history(
            @PathVariable final UUID patientId,
            @Parameter(description = "Filter by diagnosis type (PRIMARY, SECONDARY, DIFFERENTIAL)")
            @RequestParam(value = "diagnosisType", required = false) final DiagnosisType diagnosisType,
            @Parameter(description = "Filter by diagnosis status (PROVISIONAL, CONFIRMED, RULED_OUT, RESOLVED)")
            @RequestParam(value = "diagnosisStatus", required = false) final DiagnosisStatus diagnosisStatus,
            @Parameter(description = "Inclusive start date (diagnosedAt)")
            @RequestParam(value = "fromDate", required = false) final LocalDate fromDate,
            @Parameter(description = "Inclusive end date (diagnosedAt)")
            @RequestParam(value = "toDate", required = false) final LocalDate toDate,
            @PageableDefault(size = 20, sort = "diagnosedAt", direction = Sort.Direction.DESC)
            final Pageable pageable
    ) {
        final PageResponse<DiagnosisResponse> page = diagnosisService.patientHistory(
                patientId,
                diagnosisType,
                diagnosisStatus,
                fromDate,
                toDate,
                pageable
        );
        return ResponseEntity.ok(ApiResponse.success("Patient diagnosis history retrieved", page));
    }
}
