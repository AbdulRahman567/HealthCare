package com.healthcare.hms.patients.allergy.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.healthcare.hms.patients.allergy.enums.AllergyType;
import com.healthcare.hms.patients.allergy.enums.Reaction;
import com.healthcare.hms.patients.allergy.enums.Severity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Allergy clinical alert rules")
class AllergyTest {

    @Test
    @DisplayName("LIFE_THREATENING forces critical alert and banner")
    void lifeThreatening_forcesAlerts() {
        final Allergy allergy = baseAllergy();
        allergy.setSeverity(Severity.LIFE_THREATENING);
        allergy.setCriticalAlert(false);
        allergy.setShowOnBanner(false);

        allergy.applyClinicalAlertRules();

        assertThat(allergy.isCriticalAlert()).isTrue();
        assertThat(allergy.isShowOnBanner()).isTrue();
        assertThat(allergy.isLifeThreatening()).isTrue();
    }

    @Test
    @DisplayName("ANAPHYLAXIS reaction forces critical alert and banner")
    void anaphylaxis_forcesAlerts() {
        final Allergy allergy = baseAllergy();
        allergy.setSeverity(Severity.MODERATE);
        allergy.setReaction(Reaction.ANAPHYLAXIS);
        allergy.setCriticalAlert(false);
        allergy.setShowOnBanner(false);

        allergy.applyClinicalAlertRules();

        assertThat(allergy.isCriticalAlert()).isTrue();
        assertThat(allergy.isShowOnBanner()).isTrue();
        assertThat(allergy.isLifeThreatening()).isTrue();
    }

    @Test
    @DisplayName("criticalAlert alone forces banner visibility")
    void criticalFlag_forcesBanner() {
        final Allergy allergy = baseAllergy();
        allergy.setSeverity(Severity.MILD);
        allergy.setReaction(Reaction.RASH);
        allergy.setCriticalAlert(true);
        allergy.setShowOnBanner(false);

        allergy.applyClinicalAlertRules();

        assertThat(allergy.isShowOnBanner()).isTrue();
    }

    @Test
    @DisplayName("mild rash does not auto-escalate")
    void mildRash_noAutoEscalate() {
        final Allergy allergy = baseAllergy();
        allergy.setSeverity(Severity.MILD);
        allergy.setReaction(Reaction.RASH);
        allergy.setCriticalAlert(false);
        allergy.setShowOnBanner(true);

        allergy.applyClinicalAlertRules();

        assertThat(allergy.isCriticalAlert()).isFalse();
        assertThat(allergy.isShowOnBanner()).isTrue();
        assertThat(allergy.isLifeThreatening()).isFalse();
    }

    private static Allergy baseAllergy() {
        final Allergy allergy = new Allergy();
        allergy.setAllergenName("Penicillin");
        allergy.setAllergyType(AllergyType.DRUG);
        allergy.setSeverity(Severity.MODERATE);
        allergy.setReaction(Reaction.RASH);
        return allergy;
    }
}
