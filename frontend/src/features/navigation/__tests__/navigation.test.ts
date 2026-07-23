import { Permissions } from '@/features/authorization/constants/permissions';
import { PROTECTED_ROUTES } from '@/features/authorization/config/routes';
import { DASHBOARD_CARDS } from '@/features/navigation/config/dashboard-cards';
import { WORKSPACE_NAVIGATION } from '@/features/navigation/config/nav-items';
import { QUICK_ACTIONS } from '@/features/navigation/config/quick-actions';
import { filterByPermission } from '@/features/navigation/lib/filter-by-permission';
import {
  buildBreadcrumbs,
  filterSidebarNavigation,
  filterTopNavigation,
  isNavItemActive,
} from '@/features/navigation/lib/navigation';

describe('filterByPermission', () => {
  it('hides unauthorized navigation without role checks', () => {
    const items = filterSidebarNavigation([
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

  it('shows hospital when HOSPITAL_READ is granted (permission-only)', () => {
    const items = filterSidebarNavigation([
      Permissions.HOSPITAL_READ,
      Permissions.USER_READ,
      Permissions.BILLING_READ,
    ]);
    const ids = items.map((item) => item.id);
    expect(ids).toEqual(expect.arrayContaining(['hospital', 'users', 'billing']));
  });

  it('filters dashboard cards by permission', () => {
    const cards = filterByPermission(DASHBOARD_CARDS, [Permissions.PATIENT_READ]);
    const ids = cards.map((card) => card.id);
    expect(ids).toContain('patients');
    expect(ids).toContain('profile');
    expect(ids).not.toContain('billing');
    expect(ids).not.toContain('users');
  });

  it('filters quick actions by create/update permissions', () => {
    const actions = filterByPermission(QUICK_ACTIONS, [
      Permissions.PATIENT_CREATE,
      Permissions.HOSPITAL_UPDATE,
    ]);
    const ids = actions.map((action) => action.id);
    expect(ids).toContain('register-patient');
    expect(ids).toContain('edit-hospital');
    expect(ids).not.toContain('book-appointment');
    expect(ids).not.toContain('create-invoice');
  });

  it('exposes the same items for top and sidebar catalogs', () => {
    const permissions = [Permissions.USER_READ];
    expect(filterTopNavigation(permissions).map((i) => i.id)).toEqual(
      filterSidebarNavigation(permissions).map((i) => i.id),
    );
  });

  it('keeps profile when permission list is empty (dashboard requires DASHBOARD_READ)', () => {
    const items = filterByPermission(WORKSPACE_NAVIGATION, []);
    expect(items.map((item) => item.id)).toEqual(['profile']);
  });
});

describe('route and navigation catalog parity', () => {
  it('keeps every workspace nav href in PROTECTED_ROUTES with matching permissions', () => {
    for (const item of WORKSPACE_NAVIGATION) {
      expect(PROTECTED_ROUTES[item.href]).toBeDefined();
      if (item.permissions?.length) {
        expect(PROTECTED_ROUTES[item.href].permissions).toEqual(item.permissions);
      } else {
        expect(PROTECTED_ROUTES[item.href]).toEqual({});
      }
    }
  });
});

describe('buildBreadcrumbs', () => {
  it('builds dashboard trail for /app', () => {
    expect(buildBreadcrumbs('/app')).toEqual([{ label: 'Dashboard', href: '/app' }]);
  });

  it('resolves labeled crumbs from the nav catalog', () => {
    expect(buildBreadcrumbs('/app/patients')).toEqual([
      { label: 'Dashboard', href: '/app' },
      { label: 'Patients' },
    ]);
  });

  it('appends nested segments after a catalog match', () => {
    expect(buildBreadcrumbs('/app/patients/new')).toEqual([
      { label: 'Dashboard', href: '/app' },
      { label: 'Patients', href: '/app/patients' },
      { label: 'New' },
    ]);
  });
});

describe('isNavItemActive', () => {
  it('treats /app as exact-only', () => {
    expect(isNavItemActive('/app', '/app')).toBe(true);
    expect(isNavItemActive('/app/patients', '/app')).toBe(false);
  });

  it('matches nested module paths', () => {
    expect(isNavItemActive('/app/patients/new', '/app/patients')).toBe(true);
  });
});
