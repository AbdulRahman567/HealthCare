package com.healthcare.hms.security.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.healthcare.hms.security.SecurityConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AuthorityUtils")
class AuthorityUtilsTest {

    @Test
    @DisplayName("normalizes role tokens with and without ROLE_ prefix")
    void normalizeRole() {
        assertThat(AuthorityUtils.normalizeRole("doctor")).isEqualTo("DOCTOR");
        assertThat(AuthorityUtils.normalizeRole("ROLE_NURSE")).isEqualTo("NURSE");
        assertThat(AuthorityUtils.toRoleAuthority("doctor"))
                .isEqualTo(SecurityConstants.ROLE_PREFIX + "DOCTOR");
        assertThat(AuthorityUtils.toRoleAuthority("ROLE_DOCTOR"))
                .isEqualTo(SecurityConstants.ROLE_PREFIX + "DOCTOR");
        assertThat(AuthorityUtils.isRoleAuthority("ROLE_DOCTOR")).isTrue();
        assertThat(AuthorityUtils.isRoleAuthority("PATIENT_READ")).isFalse();
    }
}
