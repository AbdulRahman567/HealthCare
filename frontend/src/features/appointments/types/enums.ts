export const APPOINTMENT_STATUSES = [
  'SCHEDULED',
  'CONFIRMED',
  'COMPLETED',
  'CANCELLED',
  'MISSED',
] as const;

export type AppointmentStatus = (typeof APPOINTMENT_STATUSES)[number];

export const APPOINTMENT_TYPES = [
  'CONSULTATION',
  'FOLLOW_UP',
  'PROCEDURE',
  'EMERGENCY',
  'TELEHEALTH',
] as const;

export type AppointmentType = (typeof APPOINTMENT_TYPES)[number];

export const VISIT_TYPES = ['NEW', 'FOLLOW_UP', 'WALK_IN', 'TELECONSULTATION'] as const;

export type VisitType = (typeof VISIT_TYPES)[number];

export const QUEUE_ENTRY_STATUSES = [
  'CHECKED_IN',
  'WAITING',
  'IN_CONSULTATION',
  'COMPLETED',
  'MISSED',
  'CANCELLED',
] as const;

export type QueueEntryStatus = (typeof QUEUE_ENTRY_STATUSES)[number];

export const CALENDAR_SCOPES = ['DOCTOR', 'DEPARTMENT', 'HOSPITAL'] as const;

export type CalendarScope = (typeof CALENDAR_SCOPES)[number];

export const CALENDAR_VIEW_TYPES = ['DAILY', 'WEEKLY', 'MONTHLY'] as const;

export type CalendarViewType = (typeof CALENDAR_VIEW_TYPES)[number];
