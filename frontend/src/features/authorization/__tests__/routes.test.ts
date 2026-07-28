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

  it('requires create permission for patient registration', () => {
    expect(resolveRouteAccess('/app/patients/new')).toEqual({
      status: 'allow',
      requirement: { permissions: [Permissions.PATIENT_CREATE] },
    });
  });

  it('requires update permission for patient edit', () => {
    expect(resolveRouteAccess('/app/patients/abc/edit')).toEqual({
      status: 'allow',
      requirement: { permissions: [Permissions.PATIENT_UPDATE] },
    });
  });

  it('requires create permission for clinical start', () => {
    expect(resolveRouteAccess('/app/clinical/new')).toEqual({
      status: 'allow',
      requirement: { permissions: [Permissions.VISIT_CREATE] },
    });
  });

  it('allows clinical workspace under VISIT_READ', () => {
    expect(resolveRouteAccess('/app/clinical')).toEqual({
      status: 'allow',
      requirement: { permissions: [Permissions.VISIT_READ] },
    });
    expect(resolveRouteAccess('/app/clinical/abc')).toEqual({
      status: 'allow',
      requirement: { permissions: [Permissions.VISIT_READ] },
    });
    expect(resolveRouteAccess('/app/clinical/follow-ups')).toEqual({
      status: 'allow',
      requirement: { permissions: [Permissions.VISIT_READ] },
    });
  });

  it('denies unknown /app paths (fail-closed)', () => {
    expect(resolveRouteAccess('/app/not-registered')).toEqual({ status: 'deny' });
  });
});
