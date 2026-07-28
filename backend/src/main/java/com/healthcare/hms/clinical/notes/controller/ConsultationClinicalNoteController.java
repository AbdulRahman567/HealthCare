package com.healthcare.hms.clinical.notes.controller;

import com.healthcare.hms.clinical.enums.ClinicalNoteType;
import com.healthcare.hms.clinical.notes.dto.request.CreateClinicalNoteRequest;
import com.healthcare.hms.clinical.notes.dto.request.UpdateClinicalNoteRequest;
import com.healthcare.hms.clinical.notes.dto.response.ClinicalNoteAttachmentResponse;
import com.healthcare.hms.clinical.notes.dto.response.ClinicalNoteResponse;
import com.healthcare.hms.clinical.notes.service.ClinicalNoteAttachmentDownload;
import com.healthcare.hms.clinical.notes.service.ClinicalNoteService;
import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.common.web.ClientRequestDetails;
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
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Consultation-scoped clinical notes and attachments (Phase 7.6).
 */
@RestController
@RequestMapping("/api/v1/consultations/{consultationId}/clinical-notes")
@Validated
@Tag(
        name = "Consultation Clinical Notes",
        description = "SOAP, progress, procedure, and discharge notes with image/PDF attachments"
)
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "tenantHeader")
public class ConsultationClinicalNoteController {

    private final ClinicalNoteService clinicalNoteService;

    public ConsultationClinicalNoteController(final ClinicalNoteService clinicalNoteService) {
        this.clinicalNoteService = clinicalNoteService;
    }

    @PostMapping
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    @Operation(
            summary = "Create clinical note",
            description = """
                    Creates a structured clinical note. Supported types: SUBJECTIVE, OBJECTIVE,
                    ASSESSMENT, PLAN (SOAP), PROGRESS, PROCEDURE, DISCHARGE, ADVICE, GENERAL.
                    Writes allowed only while the consultation is editable.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Note created",
                    content = @Content(schema = @Schema(implementation = ClinicalNoteResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Consultation not found")
    })
    public ResponseEntity<ApiResponse<ClinicalNoteResponse>> create(
            @PathVariable final UUID consultationId,
            @Valid @RequestBody final CreateClinicalNoteRequest request,
            final HttpServletRequest httpRequest
    ) {
        final ClinicalNoteResponse response = clinicalNoteService.create(
                consultationId,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Clinical note created successfully", response));
    }

    @GetMapping
    @RequirePermission(PermissionConstants.VISIT_READ)
    @Operation(summary = "List clinical notes", description = "Returns notes for the consultation ordered by recordedAt ascending.")
    public ResponseEntity<ApiResponse<List<ClinicalNoteResponse>>> list(
            @PathVariable final UUID consultationId,
            @Parameter(description = "Optional filter by note type")
            @RequestParam(value = "noteType", required = false) final ClinicalNoteType noteType
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Clinical notes retrieved successfully",
                clinicalNoteService.listByConsultation(consultationId, noteType)
        ));
    }

    @GetMapping("/{noteId}")
    @RequirePermission(PermissionConstants.VISIT_READ)
    @Operation(summary = "Get clinical note", description = "Returns a note with attachment metadata. View is audit-logged.")
    public ResponseEntity<ApiResponse<ClinicalNoteResponse>> getById(
            @PathVariable final UUID consultationId,
            @PathVariable final UUID noteId,
            final HttpServletRequest httpRequest
    ) {
        final ClinicalNoteResponse response = clinicalNoteService.getById(
                consultationId,
                noteId,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Clinical note retrieved successfully", response));
    }

    @PutMapping("/{noteId}")
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    @Operation(summary = "Update clinical note", description = "Updates a note while the consultation remains editable.")
    public ResponseEntity<ApiResponse<ClinicalNoteResponse>> update(
            @PathVariable final UUID consultationId,
            @PathVariable final UUID noteId,
            @Valid @RequestBody final UpdateClinicalNoteRequest request,
            final HttpServletRequest httpRequest
    ) {
        final ClinicalNoteResponse response = clinicalNoteService.update(
                consultationId,
                noteId,
                request,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Clinical note updated successfully", response));
    }

    @DeleteMapping("/{noteId}")
    @RequirePermission(PermissionConstants.VISIT_DELETE)
    @Operation(
            summary = "Soft-delete clinical note",
            description = "Soft-deletes the note and its attachments (storage objects removed)."
    )
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable final UUID consultationId,
            @PathVariable final UUID noteId,
            final HttpServletRequest httpRequest
    ) {
        clinicalNoteService.delete(
                consultationId,
                noteId,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Clinical note deleted successfully", null));
    }

    @PostMapping(value = "/{noteId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequirePermission(PermissionConstants.VISIT_UPDATE)
    @Operation(
            summary = "Upload attachment",
            description = "Uploads an image (JPG/PNG/WEBP, max 10MB) or PDF (max 25MB) to object storage."
    )
    public ResponseEntity<ApiResponse<ClinicalNoteAttachmentResponse>> uploadAttachment(
            @PathVariable final UUID consultationId,
            @PathVariable final UUID noteId,
            @RequestParam("file") final MultipartFile file,
            @RequestParam(value = "description", required = false) final String description,
            final HttpServletRequest httpRequest
    ) {
        final ClinicalNoteAttachmentResponse response = clinicalNoteService.addAttachment(
                consultationId,
                noteId,
                file,
                description,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attachment uploaded successfully", response));
    }

    @GetMapping("/{noteId}/attachments")
    @RequirePermission(PermissionConstants.VISIT_READ)
    @Operation(summary = "List attachments", description = "Returns attachment metadata for the note.")
    public ResponseEntity<ApiResponse<List<ClinicalNoteAttachmentResponse>>> listAttachments(
            @PathVariable final UUID consultationId,
            @PathVariable final UUID noteId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Attachments retrieved successfully",
                clinicalNoteService.listAttachments(consultationId, noteId)
        ));
    }

    @GetMapping("/{noteId}/attachments/{attachmentId}")
    @RequirePermission(PermissionConstants.VISIT_READ)
    @Operation(summary = "Get attachment metadata")
    public ResponseEntity<ApiResponse<ClinicalNoteAttachmentResponse>> getAttachment(
            @PathVariable final UUID consultationId,
            @PathVariable final UUID noteId,
            @PathVariable final UUID attachmentId,
            final HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Attachment retrieved successfully",
                clinicalNoteService.getAttachmentMetadata(
                        consultationId,
                        noteId,
                        attachmentId,
                        ClientRequestDetails.resolveClientIp(httpRequest),
                        ClientRequestDetails.resolveUserAgent(httpRequest)
                )
        ));
    }

    @GetMapping("/{noteId}/attachments/{attachmentId}/download")
    @RequirePermission(PermissionConstants.VISIT_READ)
    @Operation(summary = "Download attachment", description = "Streams the binary from object storage (local / S3 / MinIO).")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable final UUID consultationId,
            @PathVariable final UUID noteId,
            @PathVariable final UUID attachmentId,
            final HttpServletRequest httpRequest
    ) {
        final ClinicalNoteAttachmentDownload download = clinicalNoteService.downloadAttachment(
                consultationId,
                noteId,
                attachmentId,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + sanitizeContentDispositionFileName(download.fileName()) + "\""
                )
                .contentType(MediaType.parseMediaType(download.contentType()))
                .contentLength(download.sizeBytes())
                .body(download.resource());
    }

    private static String sanitizeContentDispositionFileName(final String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "attachment";
        }
        return fileName.replace("\"", "").replace("\r", "").replace("\n", "").trim();
    }

    @DeleteMapping("/{noteId}/attachments/{attachmentId}")
    @RequirePermission(PermissionConstants.VISIT_DELETE)
    @Operation(summary = "Soft-delete attachment", description = "Removes metadata and deletes the object from storage.")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(
            @PathVariable final UUID consultationId,
            @PathVariable final UUID noteId,
            @PathVariable final UUID attachmentId,
            final HttpServletRequest httpRequest
    ) {
        clinicalNoteService.deleteAttachment(
                consultationId,
                noteId,
                attachmentId,
                ClientRequestDetails.resolveClientIp(httpRequest),
                ClientRequestDetails.resolveUserAgent(httpRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Attachment deleted successfully", null));
    }
}
