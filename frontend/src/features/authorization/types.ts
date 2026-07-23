/**
 * Frontend authorization types.
 *
 * Values always originate from the backend (login/profile/authorization APIs).
 * The UI never invents grants — it only mirrors them for UX.
 */

export type AuthorizationStatus = 'idle' | 'loading' | 'ready' | 'error';

export type AuthorizationContext = {
  userId: string;
  tenantId: string | null;
  email: string;
  roles: string[];
  permissions: string[];
};

export type AccessMode = 'any' | 'all';

export type AccessRequirement = {
  roles?: string[];
  permissions?: string[];
  /**
   * How multiple roles/permissions are combined within each list.
   * Defaults to `any` (OR). Role list and permission list are always ANDed together.
   */
  mode?: AccessMode;
};
