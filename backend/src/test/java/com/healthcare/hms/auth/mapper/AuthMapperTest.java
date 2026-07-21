package com.healthcare.hms.auth.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.healthcare.hms.auth.dto.response.HospitalRegistrationResponse;
import com.healthcare.hms.auth.dto.response.UserProfileResponse;
import com.healthcare.hms.auth.support.AuthTestData;
import com.healthcare.hms.hospitals.entity.Tenant;
import com.healthcare.hms.users.entity.Role;
import com.healthcare.hms.users.entity.User;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AuthMapper")
class AuthMapperTest {

    private final AuthMapper mapper = new AuthMapper() {
        @Override
        public UserProfileResponse toUserProfile(final User user) {
            return null;
        }

        @Override
        public HospitalRegistrationResponse toHospitalRegistration(final Tenant tenant) {
            return null;
        }
    };

    @Test
    @DisplayName("mapRoleTypes returns empty set for null or empty roles")
    void mapRoleTypesEmpty() {
        assertThat(mapper.mapRoleTypes(null)).isEmpty();
        assertThat(mapper.mapRoleTypes(Set.of())).isEmpty();
    }

    @Test
    @DisplayName("mapRoleTypes maps role type names")
    void mapRoleTypesValues() {
        final Role role = AuthTestData.hospitalAdminRole();
        assertThat(mapper.mapRoleTypes(Set.of(role))).containsExactly("HOSPITAL_ADMIN");
    }

    @Test
    @DisplayName("mapPermissions returns empty set for null or empty roles")
    void mapPermissionsEmpty() {
        assertThat(mapper.mapPermissions(null)).isEmpty();
        assertThat(mapper.mapPermissions(Set.of())).isEmpty();
    }

    @Test
    @DisplayName("mapPermissions flattens permission codes")
    void mapPermissionsValues() {
        final Role role = AuthTestData.hospitalAdminRole();
        assertThat(mapper.mapPermissions(Set.of(role))).containsExactly("HOSPITAL_READ");
    }
}
