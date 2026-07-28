import type { AppointmentFormValues } from '@/features/appointments/validation/appointment-schema';
import type {
  AppointmentResponse,
  CreateAppointmentPayload,
  UpdateAppointmentPayload,
} from '@/features/appointments/types/appointment';

export function normalizeTime(value: string): string {
  if (/^\d{2}:\d{2}:\d{2}$/.test(value)) {
    return value.slice(0, 5);
  }
  return value;
}

export function formatTimeLabel(value: string | null | undefined): string {
  if (!value) {
    return '—';
  }
  return normalizeTime(value);
}

export function minutesBetween(startTime: string, endTime: string): number {
  const [sh, sm] = normalizeTime(startTime).split(':').map(Number);
  const [eh, em] = normalizeTime(endTime).split(':').map(Number);
  return eh * 60 + em - (sh * 60 + sm);
}

export function toCreatePayload(values: AppointmentFormValues): CreateAppointmentPayload {
  const startTime = normalizeTime(values.startTime);
  const endTime = normalizeTime(values.endTime);
  return {
    patientId: values.patientId,
    doctorId: values.doctorId,
    departmentId: values.departmentId,
    appointmentDate: values.appointmentDate,
    startTime: `${startTime}:00`,
    endTime: `${endTime}:00`,
    durationMinutes: minutesBetween(startTime, endTime),
    appointmentType: values.appointmentType,
    visitType: values.visitType,
    notes: values.notes?.trim() ? values.notes.trim() : null,
  };
}

export function toUpdatePayload(values: AppointmentFormValues): UpdateAppointmentPayload {
  const created = toCreatePayload(values);
  return {
    doctorId: created.doctorId,
    departmentId: created.departmentId,
    appointmentDate: created.appointmentDate,
    startTime: created.startTime,
    endTime: created.endTime,
    durationMinutes: created.durationMinutes,
    appointmentType: created.appointmentType,
    visitType: created.visitType,
    notes: created.notes,
  };
}

export function appointmentToFormValues(appointment: AppointmentResponse): AppointmentFormValues {
  return {
    patientId: appointment.patientId,
    doctorId: appointment.doctorId,
    departmentId: appointment.departmentId,
    appointmentDate: appointment.appointmentDate,
    startTime: normalizeTime(appointment.startTime),
    endTime: normalizeTime(appointment.endTime),
    appointmentType: appointment.appointmentType,
    visitType: appointment.visitType,
    notes: appointment.notes ?? '',
  };
}

export function isMutableAppointmentStatus(status: string): boolean {
  return status === 'SCHEDULED' || status === 'CONFIRMED';
}

export function formatAppointmentSlot(date: string, startTime: string, endTime: string): string {
  return `${date} · ${formatTimeLabel(startTime)}–${formatTimeLabel(endTime)}`;
}
