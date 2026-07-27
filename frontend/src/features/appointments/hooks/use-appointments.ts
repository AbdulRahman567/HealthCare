import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { appointmentsApi } from '@/features/appointments/api/appointments-api';
import type {
  AppointmentListQuery,
  CancelAppointmentPayload,
  CreateAppointmentPayload,
  RescheduleAppointmentPayload,
  UpdateAppointmentPayload,
} from '@/features/appointments/types/appointment';

export const appointmentKeys = {
  all: ['appointments'] as const,
  lists: () => [...appointmentKeys.all, 'list'] as const,
  list: (query: AppointmentListQuery) => [...appointmentKeys.lists(), query] as const,
  details: () => [...appointmentKeys.all, 'detail'] as const,
  detail: (id: string) => [...appointmentKeys.details(), id] as const,
};

export function useAppointmentsQuery(query: AppointmentListQuery, enabled = true) {
  return useQuery({
    queryKey: appointmentKeys.list(query),
    queryFn: () => appointmentsApi.search(query),
    enabled,
    placeholderData: (previous) => previous,
  });
}

export function useAppointmentQuery(id: string, enabled = true) {
  return useQuery({
    queryKey: appointmentKeys.detail(id),
    queryFn: () => appointmentsApi.getById(id),
    enabled: enabled && Boolean(id),
  });
}

export function useAppointmentMutations() {
  const queryClient = useQueryClient();

  const invalidateAll = () => queryClient.invalidateQueries({ queryKey: appointmentKeys.all });
  const invalidateDetail = (id: string) =>
    queryClient.invalidateQueries({ queryKey: appointmentKeys.detail(id) });

  return {
    create: useMutation({
      mutationKey: [...appointmentKeys.all, 'create'],
      mutationFn: (payload: CreateAppointmentPayload) => appointmentsApi.create(payload),
      onSuccess: invalidateAll,
    }),
    update: useMutation({
      mutationKey: [...appointmentKeys.all, 'update'],
      mutationFn: ({ id, payload }: { id: string; payload: UpdateAppointmentPayload }) =>
        appointmentsApi.update(id, payload),
      onSuccess: (_data, variables) => {
        invalidateAll();
        invalidateDetail(variables.id);
      },
    }),
    reschedule: useMutation({
      mutationKey: [...appointmentKeys.all, 'reschedule'],
      mutationFn: ({ id, payload }: { id: string; payload: RescheduleAppointmentPayload }) =>
        appointmentsApi.reschedule(id, payload),
      onSuccess: (_data, variables) => {
        invalidateAll();
        invalidateDetail(variables.id);
      },
    }),
    cancel: useMutation({
      mutationKey: [...appointmentKeys.all, 'cancel'],
      mutationFn: ({ id, payload }: { id: string; payload?: CancelAppointmentPayload }) =>
        appointmentsApi.cancel(id, payload ?? {}),
      onSuccess: (_data, variables) => {
        invalidateAll();
        invalidateDetail(variables.id);
      },
    }),
    confirm: useMutation({
      mutationKey: [...appointmentKeys.all, 'confirm'],
      mutationFn: (id: string) => appointmentsApi.confirm(id),
      onSuccess: (_data, id) => {
        invalidateAll();
        invalidateDetail(id);
      },
    }),
  };
}
