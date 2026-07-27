import { Permissions } from '@/features/authorization/constants/permissions';
import type { AccessRequirement } from '@/features/authorization/types';

/**
 * Independent frontend route access catalog (UX guards).
 * Permission-driven only — not derived from navigation UI catalogs.
 * Backend APIs still enforce independently.
 */
export const PROTECTED_ROUTES: Record<string, AccessRequirement> = {
  '/app': {
    permissions: [Permissions.DASHBOARD_READ],
  },
  '/app/profile': {},
  '/app/departments': {
    permissions: [Permissions.DEPARTMENT_READ],
  },
  '/app/staff': {
    permissions: [Permissions.STAFF_READ, Permissions.DOCTOR_READ],
    mode: 'any',
  },
  '/app/users': {
    permissions: [Permissions.USER_READ],
  },
  '/app/invitations': {
    permissions: [Permissions.USER_READ],
  },
  '/app/hospital': {
    permissions: [Permissions.HOSPITAL_READ],
  },
  '/app/patients/new': {
    permissions: [Permissions.PATIENT_CREATE],
  },
  '/app/patients': {
    permissions: [Permissions.PATIENT_READ],
  },
  '/app/appointments/new': {
    permissions: [Permissions.APPOINTMENT_CREATE],
  },
  '/app/appointments/list': {
    permissions: [Permissions.APPOINTMENT_READ],
  },
  '/app/appointments/calendar': {
    permissions: [Permissions.APPOINTMENT_READ],
  },
  '/app/appointments/queue': {
    permissions: [Permissions.APPOINTMENT_READ],
  },
  '/app/appointments/availability': {
    permissions: [Permissions.APPOINTMENT_READ],
  },
  '/app/appointments': {
    permissions: [Permissions.APPOINTMENT_READ],
  },
  '/app/billing': {
    permissions: [Permissions.BILLING_READ],
  },
};

export type RouteAccessResolution =
  { status: 'allow'; requirement: AccessRequirement } | { status: 'deny' };

const PATIENT_EDIT_PATH = /^\/app\/patients\/[^/]+\/edit$/;
const APPOINTMENT_EDIT_PATH = /^\/app\/appointments\/[^/]+\/edit$/;

/**
 * Resolves access for a pathname under the protected app shell.
 * Unknown `/app/*` paths fail closed (deny) so new pages cannot skip the catalog.
 */
export function resolveRouteAccess(pathname: string): RouteAccessResolution {
  if (PROTECTED_ROUTES[pathname]) {
    return { status: 'allow', requirement: PROTECTED_ROUTES[pathname] };
  }

  if (PATIENT_EDIT_PATH.test(pathname)) {
    return {
      status: 'allow',
      requirement: { permissions: [Permissions.PATIENT_UPDATE] },
    };
  }

  if (APPOINTMENT_EDIT_PATH.test(pathname)) {
    return {
      status: 'allow',
      requirement: { permissions: [Permissions.APPOINTMENT_UPDATE] },
    };
  }

  const match = Object.keys(PROTECTED_ROUTES)
    .filter((route) => route !== '/app' && (pathname === route || pathname.startsWith(`${route}/`)))
    .sort((a, b) => b.length - a.length)[0];

  if (match) {
    return { status: 'allow', requirement: PROTECTED_ROUTES[match] };
  }

  if (pathname === '/app' || pathname.startsWith('/app/')) {
    return { status: 'deny' };
  }

  return { status: 'allow', requirement: {} };
}

/**
 * @deprecated Prefer {@link resolveRouteAccess}. Returns undefined for deny (legacy).
 */
export function resolveRouteRequirement(pathname: string): AccessRequirement | undefined {
  const resolved = resolveRouteAccess(pathname);
  if (resolved.status === 'deny') {
    return undefined;
  }
  return resolved.requirement;
}
