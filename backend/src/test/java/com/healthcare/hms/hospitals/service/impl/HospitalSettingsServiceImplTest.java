package com.healthcare.hms.hospitals.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.healthcare.hms.audit.enums.AuditAction;
import com.healthcare.hms.audit.service.AuditLogService;
import com.healthcare.hms.common.exception.ConflictException;
import com.healthcare.hms.common.exception.ResourceNotFoundException;
import com.healthcare.hms.hospitals.dto.request.UpdateHospitalSettingsRequest;
import com.healthcare.hms.hospitals.dto.response.HospitalSettingsResponse;
import com.healthcare.hms.hospitals.entity.Hospital;
import com.healthcare.hms.hospitals.enums.HospitalStatus;
import com.healthcare.hms.hospitals.mapper.HospitalSettingsMapper;
import com.healthcare.hms.hospitals.model.WorkingDayHours;
import com.healthcare.hms.hospitals.model.WorkingHours;
import com.healthcare.hms.hospitals.repository.HospitalRepository;
import com.healthcare.hms.security.principal.AuthenticatedUser;
import com.healthcare.hms.tenant.context.TenantContext;
import com.healthcare.hms.tenant.context.TenantContextHolder;
import com.healthcare.hms.tenant.enums.TenantStatus;
import com.healthcare.hms.tenant.enums.TenantType;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("HospitalSettingsServiceImpl")
class HospitalSettingsServiceImplTest {

    private static final String IP = "127.0.0.1";
    private static final String UA = "junit";

    @Mock
    private HospitalRepository hospitalRepository;
    @Mock
    private HospitalSettingsMapper hospitalSettingsMapper;
    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private HospitalSettingsServiceImpl service;

    private UUID tenantId;
    private UUID userId;
    private Hospital hospital;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        hospital = sampleHospital(tenantId);

        TenantContextHolder.set(new TenantContext(
                tenantId,
                "city-hospital",
                TenantType.HOSPITAL,
                TenantStatus.ACTIVE
        ));

        final AuthenticatedUser principal = new AuthenticatedUser(
                userId,
                tenantId,
                "admin@city.test",
                Set.of("HOSPITAL_ADMIN"),
                Set.of("HOSPITAL_READ", "HOSPITAL_WRITE"),
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
    @DisplayName("getSettings returns mapped response for current tenant default hospital")
    void getSettings_success() {
        final HospitalSettingsResponse expected = sampleResponse(hospital);
        when(hospitalRepository.findByTenantIdAndDefaultHospitalTrue(tenantId))
                .thenReturn(Optional.of(hospital));
        when(hospitalSettingsMapper.toResponse(hospital)).thenReturn(expected);

        final HospitalSettingsResponse actual = service.getSettings();

        assertThat(actual).isEqualTo(expected);
        verify(hospitalRepository).findByTenantIdAndDefaultHospitalTrue(tenantId);
    }

    @Test
    @DisplayName("getSettings fails when default hospital is missing")
    void getSettings_notFound() {
        when(hospitalRepository.findByTenantIdAndDefaultHospitalTrue(tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(service::getSettings)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Default hospital settings not found");
    }

    @Test
    @DisplayName("updateSettings applies changes, persists, and audits within tenant")
    void updateSettings_success() {
        final UpdateHospitalSettingsRequest request = sampleUpdateRequest();
        final HospitalSettingsResponse expected = sampleResponse(hospital);

        when(hospitalRepository.findByTenantIdAndDefaultHospitalTrue(tenantId))
                .thenReturn(Optional.of(hospital));
        when(hospitalRepository.existsByTenantIdAndNameIgnoreCaseAndIdNot(
                tenantId, request.name(), hospital.getId()))
                .thenReturn(false);
        when(hospitalRepository.save(hospital)).thenReturn(hospital);
        when(hospitalSettingsMapper.toResponse(hospital)).thenReturn(expected);

        final HospitalSettingsResponse actual = service.updateSettings(request, IP, UA);

        assertThat(actual).isEqualTo(expected);
        verify(hospitalSettingsMapper).updateEntity(request, hospital);
        verify(hospitalRepository).save(hospital);

        final ArgumentCaptor<String> oldValue = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> newValue = ArgumentCaptor.forClass(String.class);
        verify(auditLogService).record(
                eq(tenantId),
                eq(userId),
                eq("HOSPITAL"),
                eq(hospital.getId().toString()),
                eq(AuditAction.UPDATE),
                oldValue.capture(),
                newValue.capture(),
                eq(IP),
                eq(UA)
        );
        assertThat(oldValue.getValue()).contains("timezone=");
        assertThat(newValue.getValue()).contains("name=");
    }

    @Test
    @DisplayName("updateSettings rejects duplicate hospital name within tenant")
    void updateSettings_duplicateName() {
        final UpdateHospitalSettingsRequest request = sampleUpdateRequest();
        when(hospitalRepository.findByTenantIdAndDefaultHospitalTrue(tenantId))
                .thenReturn(Optional.of(hospital));
        when(hospitalRepository.existsByTenantIdAndNameIgnoreCaseAndIdNot(
                tenantId, request.name(), hospital.getId()))
                .thenReturn(true);

        assertThatThrownBy(() -> service.updateSettings(request, IP, UA))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Hospital name is already in use");

        verify(hospitalRepository, never()).save(any());
        verify(auditLogService, never()).record(
                any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    private static Hospital sampleHospital(final UUID tenantId) {
        final Hospital entity = new Hospital();
        entity.setId(UUID.randomUUID());
        entity.setTenantId(tenantId);
        entity.setName("City Hospital");
        entity.setCode("DEFAULT");
        entity.setEmail("hospital@city.test");
        entity.setPhone("+1-555-0100");
        entity.setAddress("100 Main St");
        entity.setTimezone("UTC");
        entity.setCurrency("USD");
        entity.setLanguage("en");
        entity.setDefaultHospital(true);
        entity.setStatus(HospitalStatus.ACTIVE);
        entity.setUpdatedAt(Instant.parse("2026-07-22T10:00:00Z"));
        return entity;
    }

    private static UpdateHospitalSettingsRequest sampleUpdateRequest() {
        return new UpdateHospitalSettingsRequest(
                "City Hospital Updated",
                "Regional care center",
                "https://cdn.example.com/logo.png",
                "Asia/Karachi",
                "PKR",
                "en",
                "hospital@city.test",
                "+92-300-1234567",
                null,
                "https://city.hospital.test",
                "100 Main St",
                "Karachi",
                "Sindh",
                "Pakistan",
                "75500",
                new WorkingHours(
                        new WorkingDayHours(false, "09:00", "17:00"),
                        new WorkingDayHours(false, "09:00", "17:00"),
                        new WorkingDayHours(false, "09:00", "17:00"),
                        new WorkingDayHours(false, "09:00", "17:00"),
                        new WorkingDayHours(false, "09:00", "17:00"),
                        new WorkingDayHours(true, null, null),
                        new WorkingDayHours(true, null, null)
                )
        );
    }

    private static HospitalSettingsResponse sampleResponse(final Hospital hospital) {
        return new HospitalSettingsResponse(
                hospital.getId(),
                hospital.getTenantId(),
                hospital.getName(),
                hospital.getCode(),
                hospital.getDescription(),
                hospital.getLogoUrl(),
                hospital.getTimezone(),
                hospital.getCurrency(),
                hospital.getLanguage(),
                hospital.getEmail(),
                hospital.getPhone(),
                hospital.getSecondaryPhone(),
                hospital.getWebsite(),
                hospital.getAddress(),
                hospital.getCity(),
                hospital.getStateProvince(),
                hospital.getCountry(),
                hospital.getPostalCode(),
                hospital.getWorkingHours(),
                hospital.isDefaultHospital(),
                hospital.getStatus(),
                hospital.getUpdatedAt()
        );
    }
}
