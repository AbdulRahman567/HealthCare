package com.healthcare.hms.users.rbac;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.healthcare.hms.users.constant.PermissionConstants;
import com.healthcare.hms.users.enums.PermissionAction;
import com.healthcare.hms.users.enums.PermissionGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PermissionNaming")
class PermissionNamingTest {

    @Test
    @DisplayName("builds canonical GROUP_ACTION codes")
    void code_buildsCanonicalForm() {
        assertThat(PermissionNaming.code(PermissionGroup.PATIENT, PermissionAction.READ))
                .isEqualTo(PermissionConstants.PATIENT_READ);
        assertThat(PermissionNaming.code(PermissionGroup.PRESCRIPTION, PermissionAction.CREATE))
                .isEqualTo(PermissionConstants.PRESCRIPTION_CREATE);
    }

    @Test
    @DisplayName("parses known catalog codes")
    void parse_knownCodes() {
        assertThat(PermissionNaming.parseGroup(PermissionConstants.HOSPITAL_UPDATE))
                .contains(PermissionGroup.HOSPITAL);
        assertThat(PermissionNaming.parseAction(PermissionConstants.HOSPITAL_UPDATE))
                .contains(PermissionAction.UPDATE);
        assertThat(PermissionNaming.isValid(PermissionConstants.AUDIT_READ)).isTrue();
    }

    @Test
    @DisplayName("rejects malformed codes")
    void parse_rejectsInvalid() {
        assertThat(PermissionNaming.isValid("")).isFalse();
        assertThat(PermissionNaming.isValid("PATIENT")).isFalse();
        assertThat(PermissionNaming.isValid("UNKNOWN_READ")).isFalse();
        assertThatThrownBy(() -> PermissionNaming.requireGroup("BAD"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
