package com.healthcare.hms.hospitals.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.healthcare.hms.hospitals.dto.request.UpdateHospitalSettingsRequest;
import com.healthcare.hms.hospitals.dto.response.HospitalSettingsResponse;
import com.healthcare.hms.hospitals.enums.HospitalStatus;
import com.healthcare.hms.hospitals.model.WorkingDayHours;
import com.healthcare.hms.hospitals.model.WorkingHours;
import com.healthcare.hms.hospitals.service.HospitalSettingsService;
import java.time.Instant;
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
@DisplayName("HospitalSettingsController")
class HospitalSettingsControllerTest {

    @Mock
    private HospitalSettingsService hospitalSettingsService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(new HospitalSettingsController(hospitalSettingsService))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/hospitals/settings returns 200")
    void getSettings_returns200() throws Exception {
        when(hospitalSettingsService.getSettings()).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/v1/hospitals/settings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("City Hospital"))
                .andExpect(jsonPath("$.data.timezone").value("Asia/Karachi"))
                .andExpect(jsonPath("$.data.currency").value("PKR"));
    }

    @Test
    @DisplayName("PUT /api/v1/hospitals/settings returns 200")
    void updateSettings_returns200() throws Exception {
        when(hospitalSettingsService.updateSettings(
                any(UpdateHospitalSettingsRequest.class),
                nullable(String.class),
                nullable(String.class)
        )).thenReturn(sampleResponse());

        mockMvc.perform(put("/api/v1/hospitals/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.language").value("en"));
    }

    private static UpdateHospitalSettingsRequest sampleRequest() {
        return new UpdateHospitalSettingsRequest(
                "City Hospital",
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

    private static HospitalSettingsResponse sampleResponse() {
        return new HospitalSettingsResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "City Hospital",
                "DEFAULT",
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
                null,
                true,
                HospitalStatus.ACTIVE,
                Instant.parse("2026-07-22T12:00:00Z")
        );
    }
}
