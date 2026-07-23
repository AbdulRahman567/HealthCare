import { matchesAccess } from '@/features/authorization/lib/access';
import type { AccessMode } from '@/features/authorization/types';

export type PermissionGated = {
  permissions?: string[];
  mode?: AccessMode;
};

/**
 * Filters catalog entries by backend-issued permissions only.
 */
export function filterByPermission<T extends PermissionGated>(
  items: readonly T[],
  permissions: readonly string[],
): T[] {
  return items.filter((item) =>
    matchesAccess([], permissions, {
      permissions: item.permissions,
      mode: item.mode ?? 'any',
    }),
  );
}
