/**
 * Doctor availability subdomain (Phase 6.2).
 *
 * <h2>Capabilities</h2>
 * <ul>
 *   <li>Recurring schedules — working days, hours, breaks, max appointments / day</li>
 *   <li>Unavailability — leave, holiday, emergency blocks</li>
 *   <li>Overlap prevention for ACTIVE schedule periods and unavailability ranges</li>
 *   <li>Open-ended {@code effectiveTo} for future recurring schedule sequencing</li>
 * </ul>
 *
 * <h2>Package layout</h2>
 * <pre>
 * availability/
 * ├── controller/   DoctorScheduleController, DoctorUnavailabilityController
 * ├── service/      interfaces + impl
 * ├── entity/       DoctorSchedule, Window, Break, Unavailability
 * ├── enums/
 * ├── dto/request|response
 * ├── validation/
 * ├── mapper/
 * ├── repository/
 * └── support/      overlap guard, doctor access
 * </pre>
 *
 * <p>APIs are tenant-aware via {@link com.healthcare.hms.common.persistence.TenantOwnedEntity}.
 * Booking / slot generation remains Phase 6.3+.
 */
package com.healthcare.hms.appointments.availability;
