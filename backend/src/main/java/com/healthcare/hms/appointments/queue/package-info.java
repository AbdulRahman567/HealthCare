/**
 * Daily doctor OPD queue management (Phase 6.4).
 *
 * <h2>Rules</h2>
 * <ul>
 *   <li>Exactly one {@link com.healthcare.hms.appointments.queue.entity.DoctorDayQueue}
 *       per doctor per calendar day</li>
 *   <li>Automatic monotonic {@code queueNumber} on check-in</li>
 *   <li>Entries listed chronologically by queue number</li>
 *   <li>All status events audited via {@code AuditLogService}</li>
 * </ul>
 *
 * <h2>Status flow</h2>
 * {@code CHECKED_IN → WAITING → IN_CONSULTATION → COMPLETED}
 * with {@code MISSED}/{@code CANCELLED} exits.
 */
package com.healthcare.hms.appointments.queue;
