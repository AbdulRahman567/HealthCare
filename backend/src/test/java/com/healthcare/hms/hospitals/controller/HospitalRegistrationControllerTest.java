package com.healthcare.hms.hospitals.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.healthcare.hms.auth.support.AuthTestData;
import com.healthcare.hms.hospitals.dto.request.HospitalRegistrationRequest;
import com.healthcare.hms.hospitals.dto.response.HospitalRegistrationResponse;
import com.healthcare.hms.hospitals.enums.HospitalStatus;
import com.healthcare.hms.hospitals.service.HospitalRegistrationService;
import com.healthcare.hms.tenant.enums.SubscriptionPlan;
import com.healthcare.hms.tenant.enums.TenantStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@DisplayName("HospitalRegistrationController")
class HospitalRegistrationControllerTest {

    @Mock
    private HospitalRegistrationService hospitalRegistrationService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(new HospitalRegistrationController(hospitalRegistrationService))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/hospitals/register returns 201")
    void register_returns201() throws Exception {
        when(hospitalRegistrationService.register(
                any(HospitalRegistrationRequest.class), nullable(String.class), nullable(String.class)))
                .thenReturn(new HospitalRegistrationResponse(
                        UUID.randomUUID(),
                        "city-hospital",
                        TenantStatus.PENDING,
                        UUID.randomUUID(),
                        "City Hospital",
                        "DEFAULT",
                        HospitalStatus.PENDING,
                        true,
                        "hospital@city.test",
                        null,
                        null,
                        SubscriptionPlan.BASIC,
                        UUID.randomUUID(),
                        "admin@city.test",
                        false,
                        List.of("HOSPITAL_ADMIN", "DOCTOR"),
                        Instant.now()
                ));

        mockMvc.perform(post("/api/v1/hospitals/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new HospitalRegistrationRequest(
                                "City Hospital",
                                "hospital@city.test",
                                null,
                                null,
                                SubscriptionPlan.BASIC,
                                "Jane",
                                "Admin",
                                "admin@city.test",
                                AuthTestData.STRONG_PASSWORD,
                                null
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hospitalCode").value("DEFAULT"))
                .andExpect(jsonPath("$.data.adminEmail").value("admin@city.test"));
    }
}
