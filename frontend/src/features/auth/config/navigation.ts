export type AppRole =
  | 'SUPER_ADMIN'
  | 'HOSPITAL_ADMIN'
  | 'DOCTOR'
  | 'NURSE'
  | 'RECEPTIONIST'
  | 'LAB_TECHNICIAN'
  | 'PHARMACIST'
  | 'PATIENT';

export type AppPermission =
  | 'USER_READ'
  | 'USER_WRITE'
  | 'HOSPITAL_READ'
  | 'HOSPITAL_WRITE'
  | 'DEPARTMENT_READ'
  | 'DOCTOR_READ'
  | 'PATIENT_READ'
  | 'APPOINTMENT_READ'
  | 'DASHBOARD_READ'
  | 'AUDIT_READ'
  | (string & {});

export type NavItem = {
  id: string;
  label: string;
  href: string;
  description?: string;
  roles?: AppRole[];
  permissions?: AppPermission[];
  /** When true, item is shown but marked unavailable (no business module yet). */
  comingSoon?: boolean;
};

/**
 * Role-aware navigation catalog. Clinical destinations remain placeholders in Phase 1.5.
 */
export const APP_NAVIGATION: NavItem[] = [
  {
    id: 'home',
    label: 'Home',
    href: '/app',
    description: 'Signed-in workspace home',
  },
  {
    id: 'profile',
    label: 'Profile',
    href: '/app/profile',
    description: 'Your account profile',
  },
  {
    id: 'hospital',
    label: 'Hospital',
    href: '/app/hospital',
    description: 'Hospital tenant settings',
    permissions: ['HOSPITAL_READ'],
    roles: ['HOSPITAL_ADMIN', 'SUPER_ADMIN'],
    comingSoon: true,
  },
  {
    id: 'users',
    label: 'Users',
    href: '/app/users',
    description: 'Staff directory',
    permissions: ['USER_READ'],
    comingSoon: true,
  },
  {
    id: 'patients',
    label: 'Patients',
    href: '/app/patients',
    description: 'Patient records',
    permissions: ['PATIENT_READ'],
    comingSoon: true,
  },
  {
    id: 'appointments',
    label: 'Appointments',
    href: '/app/appointments',
    description: 'Scheduling',
    permissions: ['APPOINTMENT_READ'],
    comingSoon: true,
  },
];

export function canAccessNavItem(item: NavItem, roles: string[], permissions: string[]): boolean {
  const roleOk =
    !item.roles || item.roles.length === 0 || item.roles.some((role) => roles.includes(role));
  const permissionOk =
    !item.permissions ||
    item.permissions.length === 0 ||
    item.permissions.some((permission) => permissions.includes(permission));
  return roleOk && permissionOk;
}
