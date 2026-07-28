package com.healthcare.hms.prescriptions.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.clinical.entity.Consultation;
import com.healthcare.hms.clinical.enums.ConsultationStatus;
import com.healthcare.hms.clinical.repository.ConsultationRepository;
import com.healthcare.hms.clinical.support.ConsultationActorScopeSupport;
import com.healthcare.hms.common.exception.BusinessException;
import com.healthcare.hms.patients.entity.Patient;
import com.healthcare.hms.patients.enums.PatientStatus;
import com.healthcare.hms.patients.repository.PatientRepository;
import com.healthcare.hms.prescriptions.dto.request.CancelPrescriptionRequest;
import com.healthcare.hms.prescriptions.dto.response.PrescriptionResponse;
import com.healthcare.hms.prescriptions.entity.Prescription;
import com.healthcare.hms.prescriptions.enums.PrescriptionStatus;
import com.healthcare.hms.prescriptions.mapper.PrescriptionMapper;
import com.healthcare.hms.prescriptions.repository.PrescriptionItemRepository;
import com.healthcare.hms.prescriptions.repository.PrescriptionRepository;
import com.healthcare.hms.prescriptions.support.PrescriptionLabelEnricher;
import com.healthcare.hms.prescriptions.support.PrescriptionNumberGenerator;
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
@DisplayName("PrescriptionServiceImpl")
class PrescriptionServiceImplTest {

    private static final String IP = "127.0.0.1";
    private static final String UA = "junit";

    @Mock
    private PrescriptionRepository prescriptionRepository;
    @Mock
    private PrescriptionItemRepository prescriptionItemRepository;
    @Mock
    private ConsultationRepository consultationRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private PrescriptionMapper prescriptionMapper;
    @Mock
    private PrescriptionLabelEnricher labelEnricher;
    @Mock
    private PrescriptionNumberGenerator numberGenerator;
    @Mock
    private ConsultationActorScopeSupport actorScopeSupport;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private PrescriptionServiceImpl service;

    private UUID tenantId;
    private UUID prescriptionId;
    private UUID consultationId;
    private UUID patientId;
    private UUID doctorId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        prescriptionId = UUID.randomUUID();
        consultationId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();

        TenantContextHolder.set(new TenantContext(
                tenantId,
                "city-hospital",
                TenantType.HOSPITAL,
                TenantStatus.ACTIVE
        ));

        final AuthenticatedUser principal = new AuthenticatedUser(
                UUID.randomUUID(),
                tenantId,
                "doctor@city.test",
                Set.of("DOCTOR"),
                Set.of("PRESCRIPTION_READ", "PRESCRIPTION_UPDATE"),
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
    @DisplayName("issue rejects cancelled consultation")
    void issue_cancelledConsultation_throws() {
        final Prescription prescription = draftPrescription();
        final Consultation consultation = consultation(ConsultationStatus.CANCELLED);

        when(prescriptionRepository.findByIdAndTenantId(prescriptionId, tenantId))
                .thenReturn(Optional.of(prescription));
        doNothing().when(actorScopeSupport).denyPatientPortalStaffApis();
        doNothing().when(actorScopeSupport).assertDoctorAccessible(tenantId, doctorId);
        when(consultationRepository.findByIdAndTenantId(consultationId, tenantId))
                .thenReturn(Optional.of(consultation));
        doNothing().when(actorScopeSupport).assertConsultationAccessible(tenantId, consultation);

        assertThatThrownBy(() -> service.issue(prescriptionId, IP, UA))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo("CONSULTATION_NOT_PRESCRIBABLE");

        verify(prescriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("issue rejects empty medicine lines")
    void issue_emptyItems_throws() {
        final Prescription prescription = draftPrescription();
        final Consultation consultation = consultation(ConsultationStatus.IN_PROGRESS);
        final Patient patient = activePatient();

        when(prescriptionRepository.findByIdAndTenantId(prescriptionId, tenantId))
                .thenReturn(Optional.of(prescription));
        doNothing().when(actorScopeSupport).denyPatientPortalStaffApis();
        doNothing().when(actorScopeSupport).assertDoctorAccessible(tenantId, doctorId);
        when(consultationRepository.findByIdAndTenantId(consultationId, tenantId))
                .thenReturn(Optional.of(consultation));
        doNothing().when(actorScopeSupport).assertConsultationAccessible(tenantId, consultation);
        when(patientRepository.findByIdAndTenantId(patientId, tenantId)).thenReturn(Optional.of(patient));
        when(prescriptionItemRepository.countByTenantIdAndPrescriptionId(tenantId, prescriptionId))
                .thenReturn(0L);

        assertThatThrownBy(() -> service.issue(prescriptionId, IP, UA))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo("PRESCRIPTION_EMPTY");
    }

    @Test
    @DisplayName("issue transitions DRAFT → ISSUED when consult is prescribable")
    void issue_success() {
        final Prescription prescription = draftPrescription();
        final Consultation consultation = consultation(ConsultationStatus.IN_PROGRESS);
        final Patient patient = activePatient();
        final PrescriptionResponse response = org.mockito.Mockito.mock(PrescriptionResponse.class);

        when(prescriptionRepository.findByIdAndTenantId(prescriptionId, tenantId))
                .thenReturn(Optional.of(prescription));
        doNothing().when(actorScopeSupport).denyPatientPortalStaffApis();
        doNothing().when(actorScopeSupport).assertDoctorAccessible(tenantId, doctorId);
        when(consultationRepository.findByIdAndTenantId(consultationId, tenantId))
                .thenReturn(Optional.of(consultation));
        doNothing().when(actorScopeSupport).assertConsultationAccessible(tenantId, consultation);
        when(patientRepository.findByIdAndTenantId(patientId, tenantId)).thenReturn(Optional.of(patient));
        when(prescriptionItemRepository.countByTenantIdAndPrescriptionId(tenantId, prescriptionId))
                .thenReturn(2L);
        when(prescriptionRepository.save(prescription)).thenReturn(prescription);
        when(prescriptionItemRepository.findByTenantIdAndPrescriptionIdOrderBySequenceNumberAsc(
                        tenantId, prescriptionId))
                .thenReturn(List.of());
        when(labelEnricher.enrichOne(eq(tenantId), eq(prescription), anyList())).thenReturn(response);

        final PrescriptionResponse result = service.issue(prescriptionId, IP, UA);

        assertThat(result).isSameAs(response);
        assertThat(prescription.getStatus()).isEqualTo(PrescriptionStatus.ISSUED);
        assertThat(prescription.getIssuedAt()).isNotNull();
        verify(prescriptionRepository).save(prescription);
    }

    @Test
    @DisplayName("cancel transitions ISSUED → CANCELLED")
    void cancel_success() {
        final Prescription prescription = draftPrescription();
        prescription.issue();
        final PrescriptionResponse response = org.mockito.Mockito.mock(PrescriptionResponse.class);

        when(prescriptionRepository.findByIdAndTenantId(prescriptionId, tenantId))
                .thenReturn(Optional.of(prescription));
        doNothing().when(actorScopeSupport).denyPatientPortalStaffApis();
        doNothing().when(actorScopeSupport).assertDoctorAccessible(tenantId, doctorId);
        when(prescriptionRepository.save(prescription)).thenReturn(prescription);
        when(prescriptionItemRepository.findByTenantIdAndPrescriptionIdOrderBySequenceNumberAsc(
                        tenantId, prescriptionId))
                .thenReturn(List.of());
        when(labelEnricher.enrichOne(eq(tenantId), eq(prescription), anyList())).thenReturn(response);

        final PrescriptionResponse result =
                service.cancel(prescriptionId, new CancelPrescriptionRequest("Patient request"), IP, UA);

        assertThat(result).isSameAs(response);
        assertThat(prescription.getStatus()).isEqualTo(PrescriptionStatus.CANCELLED);
        assertThat(prescription.getCancelReason()).isEqualTo("Patient request");
    }

    private Prescription draftPrescription() {
        final Prescription prescription = new Prescription();
        prescription.setPrescriptionNumber("RX-TEST-1");
        prescription.setConsultationId(consultationId);
        prescription.setHospitalId(UUID.randomUUID());
        prescription.setPatientId(patientId);
        prescription.setDoctorId(doctorId);
        prescription.setDepartmentId(UUID.randomUUID());
        prescription.setPrescriptionDate(LocalDate.now());
        prescription.setStatus(PrescriptionStatus.DRAFT);
        prescription.setId(prescriptionId);
        return prescription;
    }

    private Consultation consultation(final ConsultationStatus status) {
        final Consultation consultation = new Consultation();
        consultation.setConsultationNumber("CON-1");
        consultation.setHospitalId(UUID.randomUUID());
        consultation.setPatientId(patientId);
        consultation.setDoctorId(doctorId);
        consultation.setDepartmentId(UUID.randomUUID());
        consultation.setConsultationDate(LocalDate.now());
        consultation.setStatus(status);
        return consultation;
    }

    private Patient activePatient() {
        final Patient patient = new Patient();
        patient.setStatus(PatientStatus.ACTIVE);
        return patient;
    }
}
