package com.healthcare.hms.patients.allergy.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.patients.allergy.dto.request.UpsertAllergyRequest;
import com.healthcare.hms.patients.allergy.dto.response.AllergyBannerResponse;
import com.healthcare.hms.patients.allergy.dto.response.AllergyResponse;
import com.healthcare.hms.patients.allergy.entity.Allergy;
import com.healthcare.hms.patients.allergy.enums.AllergyStatus;
import com.healthcare.hms.patients.allergy.enums.AllergyType;
import com.healthcare.hms.patients.allergy.enums.Reaction;
import com.healthcare.hms.patients.allergy.enums.Severity;
import com.healthcare.hms.patients.allergy.mapper.AllergyMapper;
import com.healthcare.hms.patients.allergy.repository.AllergyRepository;
import com.healthcare.hms.patients.entity.Patient;
import com.healthcare.hms.patients.repository.PatientRepository;
import com.healthcare.hms.security.authorization.AuthorizationService;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.tenant.context.TenantContext;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.tenant.enums.TenantStatus;
import com.healthcare.hms.tenant.enums.TenantType;
import com.healthcare.hms.users.constant.PermissionConstants;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("AllergyServiceImpl")
class AllergyServiceImplTest {

    private static final String IP = "127.0.0.1";
    private static final String UA = "junit";

    @Mock
    private AllergyRepository allergyRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private AllergyMapper allergyMapper;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private AllergyServiceImpl service;

    private UUID tenantId;
    private UUID userId;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        patientId = UUID.randomUUID();

        TenantContextHolder.set(new TenantContext(
                tenantId,
                "city-hospital",
                TenantType.HOSPITAL,
                TenantStatus.ACTIVE
        ));

        final AuthenticatedUser principal = new AuthenticatedUser(
                userId,
                tenantId,
                "doctor@city.test",
                Set.of("DOCTOR"),
                Set.of("PATIENT_READ", "PATIENT_UPDATE", "PATIENT_DELETE"),
                1L
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("create persists allergy, applies mapper, and audits")
    void create_success() {
        final UpsertAllergyRequest request = new UpsertAllergyRequest(
                "Penicillin",
                "RXN-123",
                AllergyType.DRUG,
                Severity.LIFE_THREATENING,
                Reaction.ANAPHYLAXIS,
                AllergyStatus.ACTIVE,
                null,
                null,
                true,
                false,
                null,
                null
        );
        final Allergy saved = sampleAllergy(Severity.LIFE_THREATENING, Reaction.ANAPHYLAXIS, true, true);

        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
                .thenReturn(Optional.of(new Patient()));
        when(allergyRepository.save(any(Allergy.class))).thenAnswer(invocation -> {
            final Allergy allergy = invocation.getArgument(0);
            allergy.setId(saved.getId());
            allergy.setTenantId(tenantId);
            return allergy;
        });
        when(allergyMapper.toResponse(any(Allergy.class))).thenReturn(sampleResponse(saved));

        final AllergyResponse result = service.create(patientId, request, IP, UA);

        assertThat(result.allergenName()).isEqualTo("Penicillin");
        verify(allergyMapper).apply(eq(request), any(Allergy.class));
        verify(auditLogService).record(
                eq(tenantId),
                eq(userId),
                eq("ALLERGY"),
                eq(saved.getId().toString()),
                eq(AuditAction.CREATE),
                isNull(),
                any(),
                eq(IP),
                eq(UA)
        );
    }

    @Test
    @DisplayName("banner reports NKDA when no active drug allergies")
    void banner_nkdaInference() {
        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
                .thenReturn(Optional.of(new Patient()));
        when(allergyRepository.findByTenantIdAndPatientIdAndStatusAndShowOnBannerTrueOrderBySeverityDescAllergenNameAsc(
                tenantId, patientId, AllergyStatus.ACTIVE
        )).thenReturn(List.of());
        when(allergyRepository.countByTenantIdAndPatientIdAndStatusAndCriticalAlertTrue(
                tenantId, patientId, AllergyStatus.ACTIVE
        )).thenReturn(0L);
        when(allergyRepository.existsByTenantIdAndPatientIdAndStatusAndAllergyType(
                tenantId, patientId, AllergyStatus.ACTIVE, AllergyType.DRUG
        )).thenReturn(false);

        final AllergyBannerResponse banner = service.getBannerAlerts(patientId);

        assertThat(banner.noKnownDrugAllergies()).isTrue();
        assertThat(banner.hasActiveDrugAllergies()).isFalse();
        assertThat(banner.hasCriticalAlerts()).isFalse();
        assertThat(banner.bannerAllergies()).isEmpty();
    }

    @Test
    @DisplayName("banner surfaces critical count when critical allergies exist")
    void banner_withCritical() {
        final Allergy allergy = sampleAllergy(Severity.LIFE_THREATENING, Reaction.ANAPHYLAXIS, true, true);
        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
                .thenReturn(Optional.of(new Patient()));
        when(allergyRepository.findByTenantIdAndPatientIdAndStatusAndShowOnBannerTrueOrderBySeverityDescAllergenNameAsc(
                tenantId, patientId, AllergyStatus.ACTIVE
        )).thenReturn(List.of(allergy));
        when(allergyMapper.toResponse(allergy)).thenReturn(sampleResponse(allergy));
        when(allergyRepository.countByTenantIdAndPatientIdAndStatusAndCriticalAlertTrue(
                tenantId, patientId, AllergyStatus.ACTIVE
        )).thenReturn(1L);
        when(allergyRepository.existsByTenantIdAndPatientIdAndStatusAndAllergyType(
                tenantId, patientId, AllergyStatus.ACTIVE, AllergyType.DRUG
        )).thenReturn(true);

        final AllergyBannerResponse banner = service.getBannerAlerts(patientId);

        assertThat(banner.hasCriticalAlerts()).isTrue();
        assertThat(banner.criticalAlertCount()).isEqualTo(1);
        assertThat(banner.hasActiveDrugAllergies()).isTrue();
        assertThat(banner.noKnownDrugAllergies()).isFalse();
        assertThat(banner.bannerAllergies()).hasSize(1);
    }

    @Test
    @DisplayName("list throws when patient missing")
    void list_missingPatient() {
        when(patientRepository.findByIdAndTenantId(patientId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.list(patientId, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("update omitting criticalAlert preserves flag and does not require PATIENT_DELETE")
    void update_omittedCriticalAlert_preservesWithoutDeletePermission() {
        final UUID allergyId = UUID.randomUUID();
        final Allergy existing = sampleAllergy(Severity.MODERATE, Reaction.RASH, true, true);
        existing.setId(allergyId);

        // Explicit false would clear; null must preserve (Phase 5.10).
        final UpsertAllergyRequest request = new UpsertAllergyRequest(
                "Penicillin",
                "RXN-123",
                AllergyType.DRUG,
                Severity.MODERATE,
                Reaction.RASH,
                AllergyStatus.ACTIVE,
                null,
                "Updated note",
                true,
                false,
                null,
                null
        );

        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
                .thenReturn(Optional.of(new Patient()));
        when(allergyRepository.findByIdAndTenantIdAndPatientId(allergyId, tenantId, patientId))
                .thenReturn(Optional.of(existing));
        when(allergyRepository.save(existing)).thenReturn(existing);
        when(allergyMapper.toResponse(existing)).thenReturn(sampleResponse(existing));
        doAnswer(invocation -> {
            final UpsertAllergyRequest req = invocation.getArgument(0);
            final Allergy target = invocation.getArgument(1);
            target.setAllergenName(req.allergenName());
            target.setClinicalNotes(req.clinicalNotes());
            if (req.criticalAlert() != null) {
                target.setCriticalAlert(req.criticalAlert());
            }
            if (req.showOnBanner() != null) {
                target.setShowOnBanner(req.showOnBanner());
            }
            target.applyClinicalAlertRules();
            return null;
        }).when(allergyMapper).apply(any(), any());

        // Actor without PATIENT_DELETE
        final AuthenticatedUser limited = new AuthenticatedUser(
                userId,
                tenantId,
                "receptionist@city.test",
                Set.of("RECEPTIONIST"),
                Set.of("PATIENT_READ", "PATIENT_UPDATE"),
                1L
        );
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(limited, null, limited.getAuthorities())
        );

        final AllergyResponse result = service.update(patientId, allergyId, request, IP, UA);

        assertThat(result.criticalAlert()).isTrue();
        assertThat(existing.isCriticalAlert()).isTrue();
        verify(authorizationService, never()).hasPermission(PermissionConstants.PATIENT_DELETE);
    }

    @Test
    @DisplayName("update with explicit criticalAlert=false requires PATIENT_DELETE")
    void update_explicitClearCritical_requiresDeletePermission() {
        final UUID allergyId = UUID.randomUUID();
        final Allergy existing = sampleAllergy(Severity.MODERATE, Reaction.RASH, true, true);
        existing.setId(allergyId);

        final UpsertAllergyRequest request = new UpsertAllergyRequest(
                "Penicillin",
                null,
                AllergyType.DRUG,
                Severity.MODERATE,
                Reaction.RASH,
                AllergyStatus.ACTIVE,
                null,
                null,
                true,
                false,
                false,
                true
        );

        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
                .thenReturn(Optional.of(new Patient()));
        when(allergyRepository.findByIdAndTenantIdAndPatientId(allergyId, tenantId, patientId))
                .thenReturn(Optional.of(existing));
        when(authorizationService.hasPermission(PermissionConstants.PATIENT_DELETE)).thenReturn(false);

        assertThatThrownBy(() -> service.update(patientId, allergyId, request, IP, UA))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PATIENT_DELETE");
    }

    private Allergy sampleAllergy(
            final Severity severity,
            final Reaction reaction,
            final boolean critical,
            final boolean banner
    ) {
        final Allergy allergy = new Allergy();
        allergy.setId(UUID.randomUUID());
        allergy.setTenantId(tenantId);
        allergy.setPatientId(patientId);
        allergy.setAllergenName("Penicillin");
        allergy.setAllergyType(AllergyType.DRUG);
        allergy.setSeverity(severity);
        allergy.setReaction(reaction);
        allergy.setStatus(AllergyStatus.ACTIVE);
        allergy.setCriticalAlert(critical);
        allergy.setShowOnBanner(banner);
        return allergy;
    }

    private AllergyResponse sampleResponse(final Allergy allergy) {
        return new AllergyResponse(
                allergy.getId(),
                allergy.getPatientId(),
                allergy.getAllergenName(),
                allergy.getAllergenCode(),
                allergy.getAllergyType(),
                allergy.getSeverity(),
                allergy.getReaction(),
                allergy.getStatus(),
                allergy.getOnsetDate(),
                allergy.getClinicalNotes(),
                allergy.isVerified(),
                allergy.isPatientReported(),
                allergy.isCriticalAlert(),
                allergy.isShowOnBanner(),
                allergy.isLifeThreatening(),
                allergy.getRecordedByUserId(),
                null,
                null,
                0L
        );
    }
}
