/**
 * Appointment &amp; Scheduling domain (Phase 6).
 *
 * <h2>Phase 6.1 — Appointment domain model</h2>
 * Persistence foundation: {@link com.healthcare.hms.appointments.entity.Appointment},
 * enums, repository, Flyway {@code V25__appointments.sql}.
 *
 * <h2>Phase 6.2 — Doctor availability</h2>
 * Recurring schedules (working days/hours/breaks, max appointments/day) and
 * leave/holiday/emergency unavailability under
 * {@link com.healthcare.hms.appointments.availability}. Flyway {@code V26}.
 *
 * <h2>Phase 6.3 — Appointment booking</h2>
 * Create / update / cancel / reschedule / confirm with conflict and availability
 * guards, audit logging, and Swagger-documented REST under
 * {@code /api/v1/appointments}. Flyway {@code V27}.
 *
 * <h2>Phase 6.4 — Queue management</h2>
 * Daily doctor queues, automatic queue numbers, check-in and status updates under
 * {@link com.healthcare.hms.appointments.queue}. Flyway {@code V28}.
 *
 * <h2>Phase 6.5 — Calendar management</h2>
 * Doctor / department / hospital calendars (daily, weekly, monthly) under
 * {@link com.healthcare.hms.appointments.calendar} — paginated range queries and
 * monthly aggregates without N+1.
 *
 * <h2>Phase 6.6 — Appointment search</h2>
 * Directory search via {@link com.healthcare.hms.appointments.repository.AppointmentSpecifications}
 * (pagination, sorting, filtering). Filters: appointment number, patient name,
 * doctor, department, status, visit type, date range, queue status. Flyway {@code V29}.
 *
 * <h2>Phase 6.7 — Appointment Management UI</h2>
 * Frontend scheduling surfaces (dashboard, list, book/edit, calendar, queue).
 *
 * <h2>Phase 6.8 — Appointment reminders</h2>
 * Multi-channel reminder infrastructure under
 * {@link com.healthcare.hms.appointments.reminder}: schedule/status tracking,
 * channel dispatcher ports (email via {@code EmailSender}; SMS/push logging stubs),
 * background {@link com.healthcare.hms.appointments.reminder.scheduling.ReminderDispatchScheduler},
 * audit actions. Flyway {@code V30}. External SMS/push providers are not integrated yet.
 *
 * <h2>Architecture</h2>
 * <ul>
 *   <li>Aggregates extend {@link com.healthcare.hms.common.persistence.TenantOwnedEntity}</li>
 *   <li>Cross-module links use UUID foreign keys (no {@code @ManyToOne} across modules)</li>
 *   <li>Tenant isolation via Hibernate {@code tenantFilter}; soft-delete via {@code @SQLRestriction}</li>
 *   <li>Search filters run in MySQL via Specifications / EXISTS — never in-memory</li>
 * </ul>
 *
 * <h2>Package layout</h2>
 * <pre>
 * appointments/
 * ├── controller/      AppointmentController (6.3 booking + 6.6 search)
 * ├── service/         AppointmentService (+ impl)
 * ├── dto/             request | response (incl. AppointmentSearchCriteria)
 * ├── mapper/
 * ├── validation/
 * ├── support/         conflict / availability / number guards
 * ├── entity/          Appointment (6.1+)
 * ├── enums/
 * ├── repository/      AppointmentRepository + AppointmentSpecifications (6.6)
 * ├── availability/    Doctor schedules + unavailability (6.2)
 * ├── queue/           Daily doctor queue (6.4)
 * ├── calendar/        Daily / weekly / monthly calendars (6.5)
 * └── reminder/        Reminder scheduling + channel ports (6.8)
 * </pre>
 */
package com.healthcare.hms.appointments;
