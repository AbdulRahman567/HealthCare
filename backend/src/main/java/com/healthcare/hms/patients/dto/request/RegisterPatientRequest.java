package com.healthcare.hms.patients.dto.request;

import com.healthcare.hms.patients.enums.BloodGroup;
import com.healthcare.hms.patients.enums.Gender;
import com.healthcare.hms.patients.enums.MaritalStatus;
import com.healthcare.hms.patients.validation.ReasonableDateOfBirth;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Register a new patient within the current tenant.
 *
 * <p>Tenant is resolved server-side. Status defaults to {@code ACTIVE}.
 */
public record RegisterPatientRequest(
        @NotBlank(message = "MRN is required")
        @Size(min = 2, max = 50, message = "MRN must be between 2 and 50 characters")
        @Pattern(regexp = PatientContactPatterns.MRN, message = "MRN may contain letters, digits, underscore, and hyphen")
        String mrn,

        @NotBlank(message = "First name is required")
        @Size(min = 1, max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 1, max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @NotNull(message = "Date of birth is required")
        @PastOrPresent(message = "Date of birth must be in the past or today")
        @ReasonableDateOfBirth
        LocalDate dateOfBirth,

        @NotNull(message = "Gender is required")
        Gender gender,

        BloodGroup bloodGroup,

        @Size(max = 50, message = "National ID must not exceed 50 characters")
        String nationalId,

        @Size(max = 30, message = "Phone must not exceed 30 characters")
        @Pattern(regexp = PatientContactPatterns.PHONE_OPTIONAL, message = "Phone format is invalid")
        String phone,

        @Email(message = "Email format is invalid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @Size(max = 500, message = "Address must not exceed 500 characters")
        String address,

        @Valid
        EmergencyContactRequest emergencyContact,

        MaritalStatus maritalStatus
) {
}
