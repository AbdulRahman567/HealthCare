package com.healthcare.hms.auth.validator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("StrongPasswordValidator")
class StrongPasswordValidatorTest {

    private final StrongPasswordValidator validator = new StrongPasswordValidator();

    @Test
    @DisplayName("accepts null so @NotBlank can own presence checks")
    void nullIsValid() {
        assertThat(validator.isValid(null, null)).isTrue();
    }

    @Test
    @DisplayName("accepts password meeting policy")
    void strongPasswordIsValid() {
        assertThat(validator.isValid("StrongPass1!ab", null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Short1!",
            "alllowercase1!",
            "ALLUPPERCASE1!",
            "NoDigitHere!!",
            "NoSpecialChar1"
    })
    @DisplayName("rejects passwords that violate policy")
    void weakPasswordsAreInvalid(final String password) {
        assertThat(validator.isValid(password, null)).isFalse();
    }
}
