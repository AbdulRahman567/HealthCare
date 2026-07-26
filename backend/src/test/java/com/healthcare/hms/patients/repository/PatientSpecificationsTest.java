package com.healthcare.hms.patients.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.healthcare.hms.patients.entity.Patient;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PatientSpecifications")
class PatientSpecificationsTest {

    @Test
    @DisplayName("escapeLike escapes wildcard characters")
    void escapeLike_escapesWildcards() {
        assertThat(PatientSpecifications.escapeLike("100%_x\\y"))
                .isEqualTo("100\\%\\_x\\\\y");
    }

    @Test
    @DisplayName("age range converts to DOB predicates against indexed date_of_birth")
    @SuppressWarnings("unchecked")
    void applyAgeRange_convertsToDobBounds() {
        final CriteriaBuilder cb = mock(CriteriaBuilder.class);
        final Root<Patient> root = mock(Root.class);
        final Path<LocalDate> dobPath = mock(Path.class);
        final Predicate minPredicate = mock(Predicate.class);
        final Predicate maxPredicate = mock(Predicate.class);
        final List<Predicate> predicates = new ArrayList<>();
        final LocalDate today = LocalDate.of(2026, 7, 26);

        when(root.<LocalDate>get("dateOfBirth")).thenReturn(dobPath);
        when(cb.lessThanOrEqualTo(eq(dobPath), any(LocalDate.class))).thenReturn(minPredicate);
        when(cb.greaterThanOrEqualTo(eq(dobPath), any(LocalDate.class))).thenReturn(maxPredicate);

        PatientSpecifications.applyAgeRange(predicates, cb, root, 20, 30, today);

        assertThat(predicates).containsExactly(minPredicate, maxPredicate);
        verify(cb).lessThanOrEqualTo(dobPath, LocalDate.of(2006, 7, 26));
        verify(cb).greaterThanOrEqualTo(dobPath, LocalDate.of(1995, 7, 27));
    }
}
