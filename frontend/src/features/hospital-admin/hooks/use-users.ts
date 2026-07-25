import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { usersApi } from '@/features/hospital-admin/api/users-api';
import type {
  AdminUpdateUserPayload,
  UserLifecycleAction,
  UserListQuery,
} from '@/features/hospital-admin/types/user';

export const userKeys = {
  all: ['hospital-admin', 'users'] as const,
  lists: () => [...userKeys.all, 'list'] as const,
  list: (query: UserListQuery) => [...userKeys.lists(), query] as const,
};

export function useUsersQuery(query: UserListQuery, enabled = true) {
  return useQuery({
    queryKey: userKeys.list(query),
    queryFn: () => usersApi.list(query),
    enabled,
    placeholderData: (previous) => previous,
  });
}

export function useUserMutations() {
  const queryClient = useQueryClient();
  const invalidate = () => queryClient.invalidateQueries({ queryKey: userKeys.all });

  return {
    update: useMutation({
      mutationKey: [...userKeys.all, 'update'],
      mutationFn: ({ id, payload }: { id: string; payload: AdminUpdateUserPayload }) =>
        usersApi.update(id, payload),
      onSuccess: invalidate,
    }),
    lifecycle: useMutation({
      mutationKey: [...userKeys.all, 'lifecycle'],
      mutationFn: ({ id, action }: { id: string; action: UserLifecycleAction }) =>
        usersApi.lifecycle(id, action),
      onSuccess: invalidate,
    }),
  };
}
