package com.healthcare.hms.users.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.healthcare.hms.users.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("User status transitions")
class UserStatusTransitionTest {

    @Test
    void activate_fromPending() {
        final User user = user(UserStatus.PENDING);
        user.activate();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void deactivate_fromActive() {
        final User user = user(UserStatus.ACTIVE);
        user.deactivate();
        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    void suspend_fromActive() {
        final User user = user(UserStatus.ACTIVE);
        user.suspend();
        assertThat(user.getStatus()).isEqualTo(UserStatus.SUSPENDED);
    }

    @Test
    void restore_fromSuspended() {
        final User user = user(UserStatus.SUSPENDED);
        user.restore();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void suspend_fromInactive_throws() {
        final User user = user(UserStatus.INACTIVE);
        assertThatThrownBy(user::suspend).isInstanceOf(IllegalStateException.class);
    }

    private static User user(final UserStatus status) {
        final User user = new User();
        user.setFirstName("A");
        user.setLastName("B");
        user.setEmail("a@b.test");
        user.setPasswordHash("x");
        user.setStatus(status);
        return user;
    }
}
