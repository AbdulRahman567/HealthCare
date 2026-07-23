'use client';

import { useRoleContext } from '@/features/authorization/providers/role-provider';
import type { AccessMode } from '@/features/authorization/types';

/**
 * Role hook for UX gating. Roles are mirrored from the backend principal.
 */
export function useRole() {
  const { roles, status, hasRole, hasAllRoles } = useRoleContext();

  function canRole(role: string | string[], mode: AccessMode = 'any'): boolean {
    return mode === 'all' ? hasAllRoles(role) : hasRole(role);
  }

  return {
    roles,
    status,
    hasRole: canRole,
    hasAllRoles,
  };
}
