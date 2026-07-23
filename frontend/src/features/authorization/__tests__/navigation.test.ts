import { filterNavigation, APP_NAVIGATION } from '@/features/authorization/config/navigation';
import { Permissions } from '@/features/authorization/constants/permissions';
import { Roles } from '@/features/authorization/constants/roles';

describe('filterNavigation (compat)', () => {
  it('hides unauthorized menu items using permissions only', () => {
    const items = filterNavigation(APP_NAVIGATION, [Roles.DOCTOR], [
      Permissions.PATIENT_READ,
      Permissions.APPOINTMENT_READ,
      Permissions.DASHBOARD_READ,
    ]);

    const ids = items.map((item) => item.id);
    expect(ids).toContain('dashboard');
    expect(ids).toContain('profile');
    expect(ids).toContain('patients');
    expect(ids).toContain('appointments');
    expect(ids).not.toContain('users');
    expect(ids).not.toContain('billing');
    expect(ids).not.toContain('hospital');
  });

  it('shows hospital for HOSPITAL_READ regardless of role argument', () => {
    const items = filterNavigation(APP_NAVIGATION, [], [
      Permissions.HOSPITAL_READ,
      Permissions.USER_READ,
      Permissions.BILLING_READ,
    ]);
    const ids = items.map((item) => item.id);
    expect(ids).toEqual(expect.arrayContaining(['hospital', 'users', 'billing']));
  });
});
