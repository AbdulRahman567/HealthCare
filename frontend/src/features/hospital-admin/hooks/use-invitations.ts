import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { invitationsApi } from '@/features/hospital-admin/api/invitations-api';
import type {
  CreateInvitationPayload,
  InvitationListQuery,
} from '@/features/hospital-admin/types/invitation';

export const invitationKeys = {
  all: ['hospital-admin', 'invitations'] as const,
  lists: () => [...invitationKeys.all, 'list'] as const,
  list: (query: InvitationListQuery) => [...invitationKeys.lists(), query] as const,
};

export function useInvitationsQuery(query: InvitationListQuery, enabled = true) {
  return useQuery({
    queryKey: invitationKeys.list(query),
    queryFn: () => invitationsApi.list(query),
    enabled,
    placeholderData: (previous) => previous,
  });
}

export function useInvitationMutations() {
  const queryClient = useQueryClient();
  const invalidate = () => queryClient.invalidateQueries({ queryKey: invitationKeys.all });

  return {
    create: useMutation({
      mutationKey: [...invitationKeys.all, 'create'],
      mutationFn: (payload: CreateInvitationPayload) => invitationsApi.create(payload),
      onSuccess: invalidate,
    }),
    resend: useMutation({
      mutationKey: [...invitationKeys.all, 'resend'],
      mutationFn: (id: string) => invitationsApi.resend(id),
      onSuccess: invalidate,
    }),
    cancel: useMutation({
      mutationKey: [...invitationKeys.all, 'cancel'],
      mutationFn: (id: string) => invitationsApi.cancel(id),
      onSuccess: invalidate,
    }),
  };
}
