import { useQueries } from '@tanstack/react-query';

import { departmentsApi } from '@/features/hospital-admin/api/departments-api';
import { invitationsApi } from '@/features/hospital-admin/api/invitations-api';
import { staffApi } from '@/features/hospital-admin/api/staff-api';
import { usersApi } from '@/features/hospital-admin/api/users-api';
import { useAuthorization } from '@/features/authorization/hooks/use-authorization';
import { Permissions } from '@/features/authorization/constants/permissions';
import type { StaffType } from '@/features/hospital-admin/types/enums';

const STAFF_COUNT_TYPES: StaffType[] = [
  'DOCTOR',
  'NURSE',
  'RECEPTIONIST',
  'LABORATORY_STAFF',
  'PHARMACIST',
];

/**
 * Aggregates lightweight totals for the hospital administration dashboard.
 * Uses size=1 list calls so totalElements is available without loading full pages.
 */
export function useHospitalDashboardStats() {
  const { can } = useAuthorization();

  const canDepartments = can(Permissions.DEPARTMENT_READ);
  const canUsers = can(Permissions.USER_READ);
  const canDoctors = can(Permissions.DOCTOR_READ);
  const canStaff = can(Permissions.STAFF_READ);

  const results = useQueries({
    queries: [
      {
        queryKey: ['hospital-admin', 'dashboard', 'departments'],
        queryFn: () => departmentsApi.list({ page: 0, size: 1 }),
        enabled: canDepartments,
        staleTime: 30_000,
      },
      {
        queryKey: ['hospital-admin', 'dashboard', 'users'],
        queryFn: () => usersApi.list({ page: 0, size: 1 }),
        enabled: canUsers,
        staleTime: 30_000,
      },
      {
        queryKey: ['hospital-admin', 'dashboard', 'invitations-pending'],
        queryFn: () => invitationsApi.list({ page: 0, size: 1, status: 'PENDING' }),
        enabled: canUsers,
        staleTime: 30_000,
      },
      ...STAFF_COUNT_TYPES.map((staffType) => ({
        queryKey: ['hospital-admin', 'dashboard', 'staff', staffType],
        queryFn: () => staffApi.list(staffType, { page: 0, size: 1 }),
        enabled: staffType === 'DOCTOR' ? canDoctors : canStaff,
        staleTime: 30_000,
      })),
    ],
  });

  const [
    departmentsQuery,
    usersQuery,
    invitationsQuery,
    doctorsQuery,
    nursesQuery,
    receptionistsQuery,
    labQuery,
    pharmacistsQuery,
  ] = results;

  const staffTotal =
    (doctorsQuery.data?.totalElements ?? 0) +
    (nursesQuery.data?.totalElements ?? 0) +
    (receptionistsQuery.data?.totalElements ?? 0) +
    (labQuery.data?.totalElements ?? 0) +
    (pharmacistsQuery.data?.totalElements ?? 0);

  return {
    isLoading: results.some((result) => result.isLoading && result.fetchStatus !== 'idle'),
    departments: canDepartments ? (departmentsQuery.data?.totalElements ?? 0) : null,
    users: canUsers ? (usersQuery.data?.totalElements ?? 0) : null,
    pendingInvitations: canUsers ? (invitationsQuery.data?.totalElements ?? 0) : null,
    staff: canDoctors || canStaff ? staffTotal : null,
    doctors: canDoctors ? (doctorsQuery.data?.totalElements ?? 0) : null,
  };
}
