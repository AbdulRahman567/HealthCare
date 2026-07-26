import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { staffApi } from '@/features/hospital-admin/api/staff-api';
import type { StaffType } from '@/features/hospital-admin/types/enums';
import type { StaffListQuery } from '@/features/hospital-admin/types/staff';

export const staffKeys = {
  all: ['hospital-admin', 'staff'] as const,
  lists: (staffType: StaffType) => [...staffKeys.all, staffType, 'list'] as const,
  list: (staffType: StaffType, query: StaffListQuery) =>
    [...staffKeys.lists(staffType), query] as const,
};

export function useStaffQuery(staffType: StaffType, query: StaffListQuery, enabled = true) {
  return useQuery({
    queryKey: staffKeys.list(staffType, query),
    queryFn: () => staffApi.list(staffType, query),
    enabled,
    placeholderData: (previous) => previous,
  });
}

export function useStaffMutations(staffType: StaffType) {
  const queryClient = useQueryClient();
  const invalidate = () => queryClient.invalidateQueries({ queryKey: staffKeys.lists(staffType) });

  return {
    create: useMutation({
      mutationKey: [...staffKeys.all, staffType, 'create'],
      mutationFn: (payload: Parameters<typeof staffApi.create>[1]) =>
        staffApi.create(staffType, payload),
      onSuccess: invalidate,
    }),
    update: useMutation({
      mutationKey: [...staffKeys.all, staffType, 'update'],
      mutationFn: ({
        id,
        payload,
      }: {
        id: string;
        payload: Parameters<typeof staffApi.update>[2];
      }) => staffApi.update(staffType, id, payload),
      onSuccess: invalidate,
    }),
    remove: useMutation({
      mutationKey: [...staffKeys.all, staffType, 'remove'],
      mutationFn: (id: string) => staffApi.remove(staffType, id),
      onSuccess: invalidate,
    }),
  };
}
