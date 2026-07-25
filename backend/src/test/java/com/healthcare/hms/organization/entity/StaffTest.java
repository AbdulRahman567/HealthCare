package com.healthcare.hms.organization.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.healthcare.hms.organization.enums.EmploymentStatus;
import com.healthcare.hms.organization.enums.EmploymentType;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StaffTest {

    @Test
    @DisplayName("isOperationallyActive is true only for ACTIVE and non-deleted staff")
    void isOperationallyActive_requiresActiveAndNotDeleted() {
        final TestStaff staff = newTestStaff();
        staff.setEmploymentStatus(EmploymentStatus.ACTIVE);
        assertThat(staff.isOperationallyActive()).isTrue();

        staff.setEmploymentStatus(EmploymentStatus.ON_LEAVE);
        assertThat(staff.isOperationallyActive()).isFalse();

        staff.setEmploymentStatus(EmploymentStatus.ACTIVE);
        staff.markDeleted(UUID.randomUUID());
        assertThat(staff.isOperationallyActive()).isFalse();
    }

    @Test
    @DisplayName("default employment enums match onboarding contract")
    void defaults_arePendingFullTime() {
        final TestStaff staff = new TestStaff();
        assertThat(staff.getEmploymentStatus()).isEqualTo(EmploymentStatus.PENDING);
        assertThat(staff.getEmploymentType()).isEqualTo(EmploymentType.FULL_TIME);
    }

    private static TestStaff newTestStaff() {
        final TestStaff staff = new TestStaff();
        staff.setHospitalId(UUID.randomUUID());
        staff.setUserId(UUID.randomUUID());
        staff.setEmployeeCode("EMP-001");
        return staff;
    }

    /** Concrete stand-in solely for testing the MappedSuperclass contract. */
    private static final class TestStaff extends Staff {
    }
}
