package com.healthcare.hms.hospitals.validator;

import static org.assertj.core.api.Assertions.assertThat;

import com.healthcare.hms.hospitals.model.WorkingDayHours;
import com.healthcare.hms.hospitals.model.WorkingHours;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Hospital settings validators")
class HospitalSettingsValidatorsTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDownValidator() {
        factory.close();
    }

    @Test
    @DisplayName("ValidTimezone accepts IANA ids")
    void timezone_valid() {
        final ValidTimezoneValidator timezoneValidator = new ValidTimezoneValidator();
        assertThat(timezoneValidator.isValid("Asia/Karachi", null)).isTrue();
        assertThat(timezoneValidator.isValid("UTC", null)).isTrue();
        assertThat(timezoneValidator.isValid("Not/AZone", null)).isFalse();
    }

    @Test
    @DisplayName("ValidCurrency accepts ISO 4217 codes")
    void currency_valid() {
        final ValidCurrencyValidator currencyValidator = new ValidCurrencyValidator();
        assertThat(currencyValidator.isValid("USD", null)).isTrue();
        assertThat(currencyValidator.isValid("pkr", null)).isTrue();
        assertThat(currencyValidator.isValid("ZZZ", null)).isFalse();
    }

    @Test
    @DisplayName("ValidLanguage accepts language tags")
    void language_valid() {
        final ValidLanguageValidator languageValidator = new ValidLanguageValidator();
        assertThat(languageValidator.isValid("en", null)).isTrue();
        assertThat(languageValidator.isValid("en-US", null)).isTrue();
        assertThat(languageValidator.isValid("12", null)).isFalse();
    }

    @Test
    @DisplayName("ValidWorkingHours rejects open after close")
    void workingHours_openAfterClose() {
        final WorkingHoursHolder holder = new WorkingHoursHolder(new WorkingHours(
                new WorkingDayHours(false, "18:00", "09:00"),
                null,
                null,
                null,
                null,
                null,
                null
        ));

        final Set<ConstraintViolation<WorkingHoursHolder>> violations = validator.validate(holder);
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("ValidWorkingHours accepts closed days without times")
    void workingHours_closedDay() {
        final WorkingHoursHolder holder = new WorkingHoursHolder(new WorkingHours(
                null,
                null,
                null,
                null,
                null,
                null,
                new WorkingDayHours(true, null, null)
        ));

        assertThat(validator.validate(holder)).isEmpty();
    }

    private record WorkingHoursHolder(@ValidWorkingHours WorkingHours workingHours) {
    }
}
