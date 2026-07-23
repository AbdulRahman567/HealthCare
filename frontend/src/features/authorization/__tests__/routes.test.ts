import { Permissions } from '@/features/authorization/constants/permissions';
import { resolveRouteAccess } from '@/features/authorization/config/routes';

describe('resolveRouteAccess', () => {
  it('allows catalogued routes with their requirements', () => {
    expect(resolveRouteAccess('/app')).toEqual({
      status: 'allow',
      requirement: { permissions: [Permissions.DASHBOARD_READ] },
    });
    expect(resolveRouteAccess('/app/users')).toEqual({
      status: 'allow',
      requirement: { permissions: [Permissions.USER_READ] },
    });
  });

  it('inherits parent module permissions for nested paths', () => {
    expect(resolveRouteAccess('/app/patients/abc')).toEqual({
      status: 'allow',
      requirement: { permissions: [Permissions.PATIENT_READ] },
    });
  });

  it('denies unknown /app paths (fail-closed)', () => {
    expect(resolveRouteAccess('/app/not-registered')).toEqual({ status: 'deny' });
  });
});
