'use client';

import { usePermissionContext } from '@/features/authorization/providers/permission-provider';
import type { AccessMode } from '@/features/authorization/types';

/**
 * Permission hook for UX gating (menus, buttons, sections).
 * Backend remains the enforcement authority.
 */
export function usePermission() {
  const { permissions, status, can, canAll } = usePermissionContext();

  function canPermission(permission: string | string[], mode: AccessMode = 'any'): boolean {
    return mode === 'all' ? canAll(permission) : can(permission);
  }

  return {
    permissions,
    status,
    can: canPermission,
    canAll,
    hasPermission: canPermission,
  };
}
