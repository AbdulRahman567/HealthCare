import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { departmentsApi } from '@/features/hospital-admin/api/departments-api';
import type {
  DepartmentListQuery,
  DepartmentWritePayload,
} from '@/features/hospital-admin/types/department';

export const departmentKeys = {
  all: ['hospital-admin', 'departments'] as const,
  lists: () => [...departmentKeys.all, 'list'] as const,
  list: (query: DepartmentListQuery) => [...departmentKeys.lists(), query] as const,
  details: () => [...departmentKeys.all, 'detail'] as const,
  detail: (id: string) => [...departmentKeys.details(), id] as const,
};

export function useDepartmentsQuery(query: DepartmentListQuery, enabled = true) {
  return useQuery({
    queryKey: departmentKeys.list(query),
    queryFn: () => departmentsApi.list(query),
    enabled,
    placeholderData: (previous) => previous,
  });
}

export function useDepartmentMutation() {
  const queryClient = useQueryClient();

  const invalidate = () => queryClient.invalidateQueries({ queryKey: departmentKeys.all });

  return {
    create: useMutation({
      mutationKey: [...departmentKeys.all, 'create'],
      mutationFn: (payload: DepartmentWritePayload) => departmentsApi.create(payload),
      onSuccess: invalidate,
    }),
    update: useMutation({
      mutationKey: [...departmentKeys.all, 'update'],
      mutationFn: ({ id, payload }: { id: string; payload: DepartmentWritePayload }) =>
        departmentsApi.update(id, payload),
      onSuccess: invalidate,
    }),
    remove: useMutation({
      mutationKey: [...departmentKeys.all, 'remove'],
      mutationFn: (id: string) => departmentsApi.remove(id),
      onSuccess: invalidate,
    }),
  };
}
