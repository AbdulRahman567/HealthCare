package com.healthcare.hms.patients.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.healthcare.hms.patients.dto.request.EmergencyContactRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EmergencyContactRequestValidator")
class EmergencyContactRequestValidatorTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("empty contact is valid")
    void empty_isValid() {
        final EmergencyContactRequest request = new EmergencyContactRequest(null, null, null);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("name and phone together are valid")
    void complete_isValid() {
        final EmergencyContactRequest request = new EmergencyContactRequest(
                "Ali Ahmed",
                "+923001112233",
                "Brother"
        );
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("name without phone is invalid")
    void nameOnly_isInvalid() {
        final EmergencyContactRequest request = new EmergencyContactRequest("Ali", null, null);
        final Set<ConstraintViolation<EmergencyContactRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().contains("phone"));
    }

    @Test
    @DisplayName("phone without name is invalid")
    void phoneOnly_isInvalid() {
        final EmergencyContactRequest request = new EmergencyContactRequest(null, "+923001112233", null);
        final Set<ConstraintViolation<EmergencyContactRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().contains("name"));
    }
}
