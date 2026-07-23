/**
 * Authorization infrastructure (Phases 3.2–3.4).
 *
 * <h2>Package structure</h2>
 * <pre>
 * com.healthcare.hms.security
 * ├── annotation/
 * │   ├── RequirePermission      // permission codes
 * │   ├── RequireAuthenticated   // JWT principal only (self-service)
 * │   ├── PublicEndpoint         // anonymous OpenAPI + docs marker
 * │   ├── RequiresPermission     // legacy alias
 * │   └── RequiresRole
 * ├── authorization/
 * │   ├── PermissionGuard
 * │   ├── PermissionAuthorizationInterceptor   // controllers
 * │   ├── PermissionAuthorizationAspect        // services / other beans
 * │   ├── PermissionAnnotationSupport
 * │   ├── AuthorizationService / DefaultAuthorizationService
 * │   ├── PermissionEvaluator / DefaultPermissionEvaluator
 * │   ├── PermissionResolver / DefaultPermissionResolver
 * │   └── AccessDeniedResponses
 * ├── handler/
 * │   ├── RestAccessDeniedHandler
 * │   └── RestAuthenticationEntryPoint
 * └── config/
 *     ├── AuthorizationInfrastructureConfig
 *     └── WebMvcSecurityConfig
 * </pre>
 *
 * <h2>Usage examples</h2>
 *
 * <p><b>1. Controller — declarative only (no auth logic in method body):</b>
 * <pre>{@code
 * @RestController
 * @RequestMapping("/api/v1/patients")
 * class PatientController {
 *     @GetMapping("/{id}")
 *     @RequirePermission(PermissionConstants.PATIENT_READ)
 *     ApiResponse<PatientResponse> get(@PathVariable UUID id) {
 *         return ApiResponse.success(patientService.getById(id));
 *     }
 * }
 * }</pre>
 *
 * <p><b>2. Self-service (authenticated, no catalog permission):</b>
 * <pre>{@code
 * @GetMapping("/profile")
 * @RequireAuthenticated
 * ApiResponse<UserProfileResponse> profile() { ... }
 * }</pre>
 *
 * <p><b>3. Service method authorization:</b>
 * <pre>{@code
 * @Service
 * class PrescriptionServiceImpl implements PrescriptionService {
 *     @Override
 *     @RequirePermission(PermissionConstants.PRESCRIPTION_CREATE)
 *     public Prescription create(CreatePrescriptionCommand command) {
 *         // business rules only
 *     }
 * }
 * }</pre>
 *
 * <p><b>4. Programmatic guard (prefer annotations when possible):</b>
 * <pre>{@code
 * permissionGuard.requireAny(PermissionConstants.VISIT_DELETE);
 * if (permissionGuard.allowsAny(PermissionConstants.AUDIT_READ)) { ... }
 * }</pre>
 *
 * <p><b>5. SpEL / method security:</b>
 * <pre>{@code
 * @PreAuthorize("@authz.hasPermission('PATIENT_READ')")
 * @PreAuthorize("hasPermission(null, 'HOSPITAL_UPDATE')")
 * }</pre>
 */
package com.healthcare.hms.security.authorization;
