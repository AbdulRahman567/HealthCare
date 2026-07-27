package com.healthcare.hms.appointments.reminder.config;

import com.healthcare.hms.appointments.reminder.scheduling.ReminderDispatchScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Enables reminder properties and optional Spring scheduling for dispatch.
 */
@Configuration
@EnableConfigurationProperties(AppointmentReminderProperties.class)
public class AppointmentReminderConfiguration {

    /**
     * Nested config so {@code @EnableScheduling} is only active when the scheduler flag is on.
     */
    @Configuration
    @EnableScheduling
    @ConditionalOnProperty(prefix = "hms.reminders", name = "scheduler-enabled", havingValue = "true")
    static class ReminderSchedulingConfiguration {

        private static final Logger log = LoggerFactory.getLogger(ReminderSchedulingConfiguration.class);

        private final ReminderDispatchScheduler dispatchScheduler;

        ReminderSchedulingConfiguration(final ReminderDispatchScheduler dispatchScheduler) {
            this.dispatchScheduler = dispatchScheduler;
        }

        @Scheduled(cron = "${hms.reminders.dispatch-cron:0 */5 * * * *}")
        public void runDispatch() {
            final int attempted = dispatchScheduler.dispatchDueReminders();
            if (attempted > 0) {
                log.info("Appointment reminder dispatch run attempted={}", attempted);
            }
        }
    }
}
