package com.healthcare.hms.clinical.notes.controller;

import com.healthcare.hms.clinical.enums.ClinicalNoteType;
import com.healthcare.hms.clinical.notes.dto.response.ClinicalNoteResponse;
import com.healthcare.hms.clinical.notes.service.ClinicalNoteService;
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
 * Patient clinical-note history (cross-consultation).
 */
@RestController
@RequestMapping("/api/v1/patients/{patientId}/clinical-notes")
@Validated
@Tag(
        name = "Patient Clinical Note History",
        description = "Paginated clinical notes across consultations"
)
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class PatientClinicalNoteHistoryController {

    private final ClinicalNoteService clinicalNoteService;

    public PatientClinicalNoteHistoryController(final ClinicalNoteService clinicalNoteService) {
        this.clinicalNoteService = clinicalNoteService;
    }

    @GetMapping
    @RequirePermission(PermissionConstants.VISIT_READ)
    @Operation(
            summary = "Patient clinical note history",
            description = """
                    Paginated clinical notes for the patient, newest recordedAt first.
                    Optional filters by note type and date range. Cross-doctor visibility by patient.
                    """
    )
    public ResponseEntity<ApiResponse<PageResponse<ClinicalNoteResponse>>> history(
            @PathVariable final UUID patientId,
            @Parameter(description = "Filter by note type")
            @RequestParam(value = "noteType", required = false) final ClinicalNoteType noteType,
            @RequestParam(value = "fromDate", required = false) final LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) final LocalDate toDate,
            @PageableDefault(size = 20, sort = "recordedAt", direction = Sort.Direction.DESC)
            final Pageable pageable
    ) {
        final PageResponse<ClinicalNoteResponse> page = clinicalNoteService.patientHistory(
                patientId, noteType, fromDate, toDate, pageable);
        return ResponseEntity.ok(ApiResponse.success("Patient clinical note history retrieved", page));
    }
}
