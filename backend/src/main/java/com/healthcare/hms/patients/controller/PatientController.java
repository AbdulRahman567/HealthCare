package com.healthcare.hms.patients.controller;

import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.api.PageResponse;
import com.healthcare.hms.common.web.ClientRequestDetails;
import com.healthcare.hms.patients.dto.request.PatientSearchCriteria;
import com.healthcare.hms.patients.dto.request.RegisterPatientRequest;
import com.healthcare.hms.patients.dto.request.UpdatePatientRequest;
import com.healthcare.hms.patients.dto.response.PatientResponse;
import com.healthcare.hms.patients.enums.BloodGroup;
import com.healthcare.hms.patients.enums.Gender;
import com.healthcare.hms.patients.enums.PatientStatus;
import com.healthcare.hms.patients.service.PatientService;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.users.constant.PermissionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tenant-scoped patient registration, search, and lifecycle API (Phases 5.2 / 5.7).
 *
 * <p>No physical delete endpoints — use deactivate / reactivate.
 */
@RestController
@RequestMapping("/api/v1/patients")
@Tag(name = "Patients", description = "Patient registration, search, and lifecycle within the current tenant")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class PatientController {

    private final PatientService patientService;

    public PatientController(final PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    @RequirePermission(PermissionConstants.PATIENT_READ)
    @Operation(
            summary = "Search patients",
            description = """
                    Paginated, sorted directory search using JPA Specifications (database filters only).
                    Supports MRN, name, phone, email, CNIC/national id, status, blood group, gender,
                    date of birth / range, age range (converted to DOB predicates), department, and
                    doctor (future-ready primary_doctor_id). Free-text `q` ORs across identity fields.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Page retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid filter range"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<PageResponse<PatientResponse>>> search(
            @Parameter(description = "Free-text across MRN, name, phone, email, CNIC")
            @RequestParam(value = "q", required = false) final String q,
            @RequestParam(value = "mrn", required = false) final String mrn,
            @RequestParam(value = "firstName", required = false) final String firstName,
            @RequestParam(value = "lastName", required = false) final String lastName,
            @RequestParam(value = "phone", required = false) final String phone,
            @RequestParam(value = "email", required = false) final String email,
            @Parameter(description = "National ID / CNIC")
            @RequestParam(value = "nationalId", required = false) final String nationalId,
            @Parameter(description = "Alias for nationalId (CNIC)")
            @RequestParam(value = "cnic", required = false) final String cnic,
            @RequestParam(value = "status", required = false) final PatientStatus status,
            @RequestParam(value = "bloodGroup", required = false) final BloodGroup bloodGroup,
            @RequestParam(value = "gender", required = false) final Gender gender,
            @RequestParam(value = "dateOfBirth", required = false) final LocalDate dateOfBirth,
            @RequestParam(value = "dateOfBirthFrom", required = false) final LocalDate dateOfBirthFrom,
            @RequestParam(value = "dateOfBirthTo", required = false) final LocalDate dateOfBirthTo,
            @RequestParam(value = "ageMin", required = false) final Integer ageMin,
            @RequestParam(value = "ageMax", required = false) final Integer ageMax,
            @RequestParam(value = "departmentId", required = false) final UUID departmentId,
            @Parameter(description = "Primary doctor id (future-ready)")
            @RequestParam(value = "doctorId", required = false) final UUID doctorId,
            @PageableDefault(size = 20, sort = {"lastName", "firstName"}, direction = Sort.Direction.ASC)
            final Pageable pageable
    ) {
        final String resolvedNationalId = nationalId != null ? nationalId : cnic;
        final PatientSearchCriteria criteria = new PatientSearchCriteria(
                q,
                mrn,
                firstName,
                lastName,
                phone,
                email,
                resolvedNationalId,
                status,
                bloodGroup,
                gender,
                dateOfBirth,
                dateOfBirthFrom,
                dateOfBirthTo,
                ageMin,
                ageMax,
                departmentId,
                doctorId
        );
        return ResponseEntity.ok(ApiResponse.success(
                "Patients retrieved successfully",
                patientService.search(criteria, pageable)
        ));
    }

    @PostMapping
    @RequirePermission(PermissionConstants.PATIENT_CREATE)
    @Operation(
            summary = "Register patient",
            description = """
                    Requires JWT, tenant context, and PATIENT_CREATE.
                    Creates a patient registration with unique MRN per tenant.
                    Status defaults to ACTIVE. No physical deletion is supported later —
                    use deactivate / reactivate for lifecycle.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Patient registered",
                    content = @Content(schema = @Schema(implementation = PatientResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate MRN or national ID")
    })
    public ResponseEntity<ApiResponse<PatientResponse>> register(
            @Valid @RequestBody final RegisterPatientRequest request,
            final HttpServletRequest httpRequest
    ) {
        final PatientResponse response = patientService.register(
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Patient registered successfully", response));
    }

    @GetMapping("/{id}")
    @RequirePermission(PermissionConstants.PATIENT_READ)
    @Operation(summary = "Get patient by id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Patient retrieved",
                    content = @Content(schema = @Schema(implementation = PatientResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ApiResponse<PatientResponse>> getById(
            @PathVariable("id") final UUID id,
            final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Patient retrieved successfully",
                patientService.getById(
                        id,
                        ClientRequestDetails.resolveClientIp(httpRequest),
                        ClientRequestDetails.resolveUserAgent(httpRequest)
                )
        ));
    }

    @PutMapping("/{id}")
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    @Operation(
            summary = "Update patient",
            description = """
                    Full-replace demographics update. Does not change lifecycle status —
                    use deactivate / reactivate. MRN must remain unique per tenant.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Patient updated",
                    content = @Content(schema = @Schema(implementation = PatientResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate MRN or national ID")
    })
    public ResponseEntity<ApiResponse<PatientResponse>> update(
            @PathVariable("id") final UUID id,
            @Valid @RequestBody final UpdatePatientRequest request,
            final HttpServletRequest httpRequest
    ) {
        final PatientResponse response = patientService.update(
                id,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Patient updated successfully", response));
    }

    @PostMapping("/{id}/deactivate")
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    @Operation(
            summary = "Deactivate patient",
            description = "ACTIVE → INACTIVE. Chart retained. No physical deletion."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Patient deactivated",
                    content = @Content(schema = @Schema(implementation = PatientResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ApiResponse<PatientResponse>> deactivate(
            @PathVariable("id") final UUID id,
            final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Patient deactivated successfully",
                patientService.deactivate(
                        id,
                        ClientRequestDetails.resolveClientIp(httpRequest),
                        ClientRequestDetails.resolveUserAgent(httpRequest)
                )
        ));
    }

    @PostMapping("/{id}/reactivate")
    @RequirePermission(PermissionConstants.PATIENT_UPDATE)
    @Operation(
            summary = "Reactivate patient",
            description = "INACTIVE → ACTIVE. Restores eligibility for clinical workflows."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Patient reactivated",
                    content = @Content(schema = @Schema(implementation = PatientResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ApiResponse<PatientResponse>> reactivate(
            @PathVariable("id") final UUID id,
            final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Patient reactivated successfully",
                patientService.reactivate(
                        id,
                        ClientRequestDetails.resolveClientIp(httpRequest),
                        ClientRequestDetails.resolveUserAgent(httpRequest)
                )
        ));
    }
}
