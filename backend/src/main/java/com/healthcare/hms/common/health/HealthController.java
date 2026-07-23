package com.healthcare.hms.common.health;

import com.healthcare.hms.common.api.ApiResponse;
import com.healthcare.hms.security.annotation.PublicEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
@Tag(name = "System", description = "Liveness and platform health probes")
public class HealthController {

    @GetMapping("/health")
    @PublicEndpoint
    @SecurityRequirements
    @Operation(
            summary = "Application health probe",
            description = "Public. Returns a simple UP status for load balancers and local checks."
    )
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.success("Service is healthy", Map.of("status", "UP"));
    }
}
