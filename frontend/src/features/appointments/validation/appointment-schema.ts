import { z } from 'zod';

import { APPOINTMENT_TYPES, VISIT_TYPES } from '@/features/appointments/types/enums';

function parseTimeToMinutes(value: string): number | null {
  const match = /^([01]\d|2[0-3]):([0-5]\d)(?::([0-5]\d))?$/.exec(value);
  if (!match) {
    return null;
  }
  return Number(match[1]) * 60 + Number(match[2]);
}

export const appointmentFormSchema = z
  .object({
    patientId: z.string().uuid('Select a patient'),
    doctorId: z.string().uuid('Select a doctor'),
    departmentId: z.string().uuid('Select a department'),
    appointmentDate: z.string().min(1, 'Date is required'),
    startTime: z.string().regex(/^([01]\d|2[0-3]):[0-5]\d$/, 'Start time is required (HH:mm)'),
    endTime: z.string().regex(/^([01]\d|2[0-3]):[0-5]\d$/, 'End time is required (HH:mm)'),
    appointmentType: z.enum(APPOINTMENT_TYPES),
    visitType: z.enum(VISIT_TYPES),
    notes: z.string().trim().max(2000).optional().or(z.literal('')),
  })
  .superRefine((values, ctx) => {
    const start = parseTimeToMinutes(values.startTime);
    const end = parseTimeToMinutes(values.endTime);
    if (start === null || end === null) {
      return;
    }
    if (end <= start) {
      ctx.addIssue({
        code: 'custom',
        path: ['endTime'],
        message: 'End time must be after start time',
      });
      return;
    }
    const duration = end - start;
    if (duration < 5 || duration > 480) {
      ctx.addIssue({
        code: 'custom',
        path: ['endTime'],
        message: 'Duration must be between 5 and 480 minutes',
      });
    }
  });

export type AppointmentFormValues = z.infer<typeof appointmentFormSchema>;

export function emptyAppointmentForm(): AppointmentFormValues {
  return {
    patientId: '',
    doctorId: '',
    departmentId: '',
    appointmentDate: new Date().toISOString().slice(0, 10),
    startTime: '09:00',
    endTime: '09:30',
    appointmentType: 'CONSULTATION',
    visitType: 'NEW',
    notes: '',
  };
}

export const cancelAppointmentSchema = z.object({
  reason: z.string().trim().max(500).optional().or(z.literal('')),
});

export type CancelAppointmentFormValues = z.infer<typeof cancelAppointmentSchema>;
