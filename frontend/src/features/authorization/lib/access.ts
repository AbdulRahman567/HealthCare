import type { AccessMode, AccessRequirement } from '@/features/authorization/types';

function toList(value: string | string[] | undefined): string[] {
  if (!value) {
    return [];
  }
  return Array.isArray(value) ? value : [value];
}

/**
 * Pure access helpers. Inputs must be backend-issued role/permission codes.
 */
export function hasAny(granted: readonly string[], required: string | string[] | undefined): boolean {
  const needed = toList(required);
  if (needed.length === 0) {
    return true;
  }
  const set = new Set(granted);
  return needed.some((code) => set.has(code));
}

export function hasAll(granted: readonly string[], required: string | string[] | undefined): boolean {
  const needed = toList(required);
  if (needed.length === 0) {
    return true;
  }
  const set = new Set(granted);
  return needed.every((code) => set.has(code));
}

export function matchesAccess(
  grantedRoles: readonly string[],
  grantedPermissions: readonly string[],
  requirement: AccessRequirement | undefined,
): boolean {
  if (!requirement) {
    return true;
  }
  const mode: AccessMode = requirement.mode ?? 'any';
  const roleMatcher = mode === 'all' ? hasAll : hasAny;
  const permissionMatcher = mode === 'all' ? hasAll : hasAny;
  return (
    roleMatcher(grantedRoles, requirement.roles) &&
    permissionMatcher(grantedPermissions, requirement.permissions)
  );
}
