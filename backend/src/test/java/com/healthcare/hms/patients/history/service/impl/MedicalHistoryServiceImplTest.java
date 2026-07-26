package com.healthcare.hms.patients.history.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.patients.entity.Patient;
import com.healthcare.hms.patients.history.dto.request.UpsertPastDiseaseRequest;
import com.healthcare.hms.patients.history.dto.response.PastDiseaseResponse;
import com.healthcare.hms.patients.history.entity.MedicalHistory;
import com.healthcare.hms.patients.history.entity.PastDisease;
import com.healthcare.hms.patients.history.enums.ClinicalConditionStatus;
import com.healthcare.hms.patients.history.enums.ClinicalSeverity;
import com.healthcare.hms.patients.history.enums.DiseaseCategory;
import com.healthcare.hms.patients.history.mapper.MedicalHistoryMapper;
import com.healthcare.hms.patients.history.repository.ChronicConditionRepository;
import com.healthcare.hms.patients.history.repository.MedicalHistoryRepository;
import com.healthcare.hms.patients.history.repository.PastDiseaseRepository;
import com.healthcare.hms.patients.history.repository.SurgeryHistoryRepository;
import com.healthcare.hms.patients.repository.PatientRepository;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.tenant.context.TenantContext;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.tenant.enums.TenantStatus;
import com.healthcare.hms.tenant.enums.TenantType;
import java.time.LocalDate;
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
@DisplayName("MedicalHistoryServiceImpl")
class MedicalHistoryServiceImplTest {

    private static final String IP = "127.0.0.1";
    private static final String UA = "junit";

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private MedicalHistoryRepository medicalHistoryRepository;
    @Mock
    private PastDiseaseRepository pastDiseaseRepository;
    @Mock
    private SurgeryHistoryRepository surgeryHistoryRepository;
    @Mock
    private ChronicConditionRepository chronicConditionRepository;
    @Mock
    private MedicalHistoryMapper medicalHistoryMapper;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private MedicalHistoryServiceImpl service;

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
    @DisplayName("getByPatientId returns empty collections when no history row")
    void get_emptyHistory() {
        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
                .thenReturn(Optional.of(new Patient()));
        when(medicalHistoryRepository.findByTenantIdAndPatientId(tenantId, patientId))
                .thenReturn(Optional.empty());

        final var response = service.getByPatientId(patientId);

        assertThat(response.patientId()).isEqualTo(patientId);
        assertThat(response.id()).isNull();
        assertThat(response.pastDiseases()).isEmpty();
        assertThat(response.surgeries()).isEmpty();
        assertThat(response.chronicConditions()).isEmpty();
    }

    @Test
    @DisplayName("getByPatientId throws when patient missing")
    void get_missingPatient() {
        when(patientRepository.findByIdAndTenantId(patientId, tenantId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByPatientId(patientId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("addPastDisease creates history root, persists entry, and audits")
    void addPastDisease_success() {
        final UpsertPastDiseaseRequest request = new UpsertPastDiseaseRequest(
                "Typhoid",
                DiseaseCategory.INFECTIOUS,
                "A01.0",
                LocalDate.of(2020, 3, 1),
                LocalDate.of(2020, 4, 1),
                ClinicalSeverity.MODERATE,
                ClinicalConditionStatus.RECOVERED,
                "Resolved after antibiotics"
        );
        final MedicalHistory history = new MedicalHistory();
        history.setId(UUID.randomUUID());
        history.setTenantId(tenantId);
        history.setPatientId(patientId);

        final PastDisease saved = new PastDisease();
        saved.setId(UUID.randomUUID());
        saved.setTenantId(tenantId);
        saved.setPatientId(patientId);
        saved.setMedicalHistoryId(history.getId());

        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
                .thenReturn(Optional.of(new Patient()));
        when(medicalHistoryRepository.findByTenantIdAndPatientId(tenantId, patientId))
                .thenReturn(Optional.of(history));
        when(pastDiseaseRepository.save(any(PastDisease.class))).thenReturn(saved);
        when(medicalHistoryRepository.save(history)).thenReturn(history);
        when(medicalHistoryMapper.toPastDiseaseResponse(saved))
                .thenReturn(new PastDiseaseResponse(
                        saved.getId(),
                        patientId,
                        history.getId(),
                        "Typhoid",
                        DiseaseCategory.INFECTIOUS,
                        "A01.0",
                        LocalDate.of(2020, 3, 1),
                        LocalDate.of(2020, 4, 1),
                        ClinicalSeverity.MODERATE,
                        ClinicalConditionStatus.RECOVERED,
                        "Resolved after antibiotics",
                        userId,
                        null,
                        null,
                        0L
                ));

        final PastDiseaseResponse result = service.addPastDisease(patientId, request, IP, UA);

        assertThat(result.diseaseName()).isEqualTo("Typhoid");
        verify(medicalHistoryMapper).applyPastDisease(eq(request), any(PastDisease.class));
        verify(auditLogService).record(
                eq(tenantId),
                eq(userId),
                eq("PAST_DISEASE"),
                eq(saved.getId().toString()),
                eq(AuditAction.CREATE),
                isNull(),
                any(),
                eq(IP),
                eq(UA)
        );
    }

    @Test
    @DisplayName("addPastDisease rejects RECOVERED without recovery date")
    void addPastDisease_recoveredWithoutDate_throws() {
        final UpsertPastDiseaseRequest request = new UpsertPastDiseaseRequest(
                "Typhoid",
                DiseaseCategory.INFECTIOUS,
                null,
                LocalDate.of(2020, 3, 1),
                null,
                ClinicalSeverity.MILD,
                ClinicalConditionStatus.RECOVERED,
                null
        );

        assertThatThrownBy(() -> service.addPastDisease(patientId, request, IP, UA))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Recovery date");
        verify(pastDiseaseRepository, never()).save(any());
    }

    @Test
    @DisplayName("removePastDisease soft-deletes and audits")
    void removePastDisease_softDeletes() {
        final PastDisease entry = new PastDisease();
        entry.setId(UUID.randomUUID());
        entry.setTenantId(tenantId);
        entry.setPatientId(patientId);
        entry.setDiseaseName("Flu");
        entry.setDiseaseCategory(DiseaseCategory.INFECTIOUS);
        entry.setDiagnosisDate(LocalDate.of(2019, 1, 1));
        entry.setSeverity(ClinicalSeverity.MILD);
        entry.setConditionStatus(ClinicalConditionStatus.RECOVERED);

        when(pastDiseaseRepository.findByIdAndTenantIdAndPatientId(entry.getId(), tenantId, patientId))
                .thenReturn(Optional.of(entry));
        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
                .thenReturn(Optional.of(new Patient()));
        when(pastDiseaseRepository.save(entry)).thenReturn(entry);
        when(medicalHistoryRepository.findByTenantIdAndPatientId(tenantId, patientId))
                .thenReturn(Optional.empty());

        service.removePastDisease(patientId, entry.getId(), IP, UA);

        assertThat(entry.isDeleted()).isTrue();
        verify(auditLogService).record(
                eq(tenantId),
                eq(userId),
                eq("PAST_DISEASE"),
                eq(entry.getId().toString()),
                eq(AuditAction.DELETE),
                any(),
                any(),
                eq(IP),
                eq(UA)
        );
    }

    @Test
    @DisplayName("get loads all structured collections")
    void get_withEntries() {
        final MedicalHistory history = new MedicalHistory();
        history.setId(UUID.randomUUID());
        history.setTenantId(tenantId);
        history.setPatientId(patientId);

        when(patientRepository.findByIdAndTenantId(patientId, tenantId))
                .thenReturn(Optional.of(new Patient()));
        when(medicalHistoryRepository.findByTenantIdAndPatientId(tenantId, patientId))
                .thenReturn(Optional.of(history));
        when(pastDiseaseRepository.findByTenantIdAndPatientIdOrderByDiagnosisDateDesc(tenantId, patientId))
                .thenReturn(List.of());
        when(surgeryHistoryRepository.findByTenantIdAndPatientIdOrderByDiagnosisDateDesc(tenantId, patientId))
                .thenReturn(List.of());
        when(chronicConditionRepository.findByTenantIdAndPatientIdOrderByDiagnosisDateDesc(tenantId, patientId))
                .thenReturn(List.of());
        when(medicalHistoryMapper.toMedicalHistoryResponse(eq(history), any(), any(), any()))
                .thenReturn(new com.healthcare.hms.patients.history.dto.response.MedicalHistoryResponse(
                        history.getId(),
                        patientId,
                        null,
                        null,
                        List.of(),
                        List.of(),
                        List.of(),
                        null,
                        null,
                        0L
                ));

        final var response = service.getByPatientId(patientId);
        assertThat(response.id()).isEqualTo(history.getId());
    }
}
