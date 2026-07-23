package com.healthcare.hms.security.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.healthcare.hms.common.exception.GlobalExceptionHandler;
import com.healthcare.hms.common.exception.auth.UnauthorizedException;
import com.healthcare.hms.common.exception.authorization.MissingAuthorizationAnnotationException;
import com.healthcare.hms.common.exception.authorization.PermissionDeniedException;
import com.healthcare.hms.security.annotation.PublicEndpoint;
import com.healthcare.hms.security.annotation.RequireAuthenticated;
import com.healthcare.hms.security.annotation.RequirePermission;
import com.healthcare.hms.security.authorization.PermissionAnnotationSupport.PermissionRequirement;
import com.healthcare.hms.users.constant.PermissionConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionAuthorizationInterceptor")
class PermissionAuthorizationInterceptorTest {

    @Mock
    private PermissionGuard permissionGuard;

    @InjectMocks
    private PermissionAuthorizationInterceptor interceptor;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMvc() {
        final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(new SampleController())
                .addInterceptors(interceptor)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("skips non-handler methods")
    void skipsNonHandler() {
        final boolean proceed = interceptor.preHandle(
                mock(HttpServletRequest.class),
                mock(HttpServletResponse.class),
                new Object()
        );
        assertThat(proceed).isTrue();
        verifyNoInteractions(permissionGuard);
    }

    @Test
    @DisplayName("enforces RequirePermission on annotated controller method")
    void enforcesAnnotatedMethod() throws Exception {
        final HandlerMethod handlerMethod =
                new HandlerMethod(new SampleController(), SampleController.class.getMethod("read"));

        final boolean proceed = interceptor.preHandle(
                mock(HttpServletRequest.class),
                mock(HttpServletResponse.class),
                handlerMethod
        );

        assertThat(proceed).isTrue();
        verify(permissionGuard).requireAuthenticated();
        verify(permissionGuard).enforce(ArgumentMatchers.any(PermissionRequirement.class));
    }

    @Test
    @DisplayName("enforces RequireAuthenticated on self-service methods")
    void enforcesAuthenticatedOnly() throws Exception {
        final HandlerMethod handlerMethod =
                new HandlerMethod(new SampleController(), SampleController.class.getMethod("profile"));

        final boolean proceed = interceptor.preHandle(
                mock(HttpServletRequest.class),
                mock(HttpServletResponse.class),
                handlerMethod
        );

        assertThat(proceed).isTrue();
        verify(permissionGuard).requireAuthenticated();
    }

    @Test
    @DisplayName("allows PublicEndpoint without authentication")
    void allowsPublicEndpoint() throws Exception {
        final HandlerMethod handlerMethod =
                new HandlerMethod(new SampleController(), SampleController.class.getMethod("publicHealth"));

        final boolean proceed = interceptor.preHandle(
                mock(HttpServletRequest.class),
                mock(HttpServletResponse.class),
                handlerMethod
        );

        assertThat(proceed).isTrue();
        verifyNoInteractions(permissionGuard);
    }

    @Test
    @DisplayName("denies unclassified handlers (fail-closed)")
    void deniesUnclassifiedHandler() throws Exception {
        final HandlerMethod handlerMethod =
                new HandlerMethod(new SampleController(), SampleController.class.getMethod("open"));

        assertThatThrownBy(() -> interceptor.preHandle(
                mock(HttpServletRequest.class),
                mock(HttpServletResponse.class),
                handlerMethod
        )).isInstanceOf(MissingAuthorizationAnnotationException.class);

        verifyNoInteractions(permissionGuard);
    }

    @Test
    @DisplayName("missing JWT on RequireAuthenticated returns 401")
    void authenticatedOnly_missingAuth_returns401() throws Exception {
        doThrow(new UnauthorizedException()).when(permissionGuard).requireAuthenticated();

        mockMvc.perform(get("/sample/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Authentication is required to access this resource"));
    }

    @Test
    @DisplayName("missing permission returns generic 403 without leaking codes")
    void permissionDenied_returnsGeneric403() throws Exception {
        doThrow(new PermissionDeniedException(Set.of(PermissionConstants.HOSPITAL_READ), false))
                .when(permissionGuard)
                .enforce(ArgumentMatchers.any(PermissionRequirement.class));

        mockMvc.perform(get("/sample/read"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value(AccessDeniedResponses.ERROR_CODE))
                .andExpect(jsonPath("$.message").value(AccessDeniedResponses.MESSAGE))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("HOSPITAL_READ"))));
    }

    @Test
    @DisplayName("unclassified handler returns generic 403")
    void unclassifiedHandler_returnsGeneric403() throws Exception {
        mockMvc.perform(get("/sample/open"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value(AccessDeniedResponses.ERROR_CODE));
    }

    @RestController
    @RequestMapping("/sample")
    static class SampleController {
        @GetMapping("/read")
        @RequirePermission(PermissionConstants.PATIENT_READ)
        public void read() {
        }

        @GetMapping("/profile")
        @RequireAuthenticated
        public void profile() {
        }

        @GetMapping("/public")
        @PublicEndpoint
        public void publicHealth() {
        }

        @GetMapping("/open")
        public void open() {
        }
    }
}
