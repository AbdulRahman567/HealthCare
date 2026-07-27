import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { queueApi } from '@/features/appointments/api/queue-api';
import type {
  CheckInQueuePayload,
  QueueAction,
  QueueStatusUpdatePayload,
} from '@/features/appointments/types/queue';
import { appointmentKeys } from '@/features/appointments/hooks/use-appointments';

export const queueKeys = {
  all: ['queues'] as const,
  doctorDay: (doctorId: string, date?: string) =>
    [...queueKeys.all, 'doctor-day', doctorId, date ?? 'today'] as const,
  detail: (queueId: string) => [...queueKeys.all, 'detail', queueId] as const,
  entry: (entryId: string) => [...queueKeys.all, 'entry', entryId] as const,
};

export function useDoctorDayQueueQuery(doctorId: string, date?: string, enabled = true) {
  return useQuery({
    queryKey: queueKeys.doctorDay(doctorId, date),
    queryFn: () => queueApi.getDoctorDayQueue(doctorId, date),
    enabled: enabled && Boolean(doctorId),
    placeholderData: (previous) => previous,
  });
}

export function useQueueMutations() {
  const queryClient = useQueryClient();

  const invalidateQueues = () => queryClient.invalidateQueries({ queryKey: queueKeys.all });
  const invalidateAppointments = () =>
    queryClient.invalidateQueries({ queryKey: appointmentKeys.all });

  return {
    checkIn: useMutation({
      mutationKey: [...queueKeys.all, 'check-in'],
      mutationFn: (payload: CheckInQueuePayload) => queueApi.checkIn(payload),
      onSuccess: () => {
        invalidateQueues();
        invalidateAppointments();
      },
    }),
    updateStatus: useMutation({
      mutationKey: [...queueKeys.all, 'status'],
      mutationFn: ({
        entryId,
        action,
        payload,
      }: {
        entryId: string;
        action: QueueAction;
        payload?: QueueStatusUpdatePayload;
      }) => queueApi.updateStatus(entryId, action, payload ?? {}),
      onSuccess: () => {
        invalidateQueues();
        invalidateAppointments();
      },
    }),
  };
}
