import { Permissions } from '@/features/authorization/constants/permissions';
import type { WorkspaceNavItem } from '@/features/navigation/types';

/**
 * Single source of truth for sidebar + top navigation.
 * Items without permissions are visible to every authenticated user.
 * Access is permission-driven only (no role checks).
 */
export const WORKSPACE_NAVIGATION: WorkspaceNavItem[] = [
  {
    id: 'dashboard',
    label: 'Dashboard',
    href: '/app',
    description: 'Hospital administration overview',
    permissions: [Permissions.DASHBOARD_READ],
    icon: 'layout-dashboard',
    section: 'main',
  },
  {
    id: 'departments',
    label: 'Departments',
    href: '/app/departments',
    description: 'Hospital departments',
    permissions: [Permissions.DEPARTMENT_READ],
    icon: 'building-2',
    section: 'admin',
  },
  {
    id: 'staff',
    label: 'Staff',
    href: '/app/staff',
    description: 'Staff employment directory',
    permissions: [Permissions.STAFF_READ, Permissions.DOCTOR_READ],
    mode: 'any',
    icon: 'stethoscope',
    section: 'admin',
  },
  {
    id: 'users',
    label: 'Users',
    href: '/app/users',
    description: 'User accounts and status',
    permissions: [Permissions.USER_READ],
    icon: 'users',
    section: 'admin',
  },
  {
    id: 'invitations',
    label: 'Invitations',
    href: '/app/invitations',
    description: 'Staff invitation management',
    permissions: [Permissions.USER_READ],
    icon: 'mail-plus',
    section: 'admin',
  },
  {
    id: 'hospital',
    label: 'Hospital',
    href: '/app/hospital',
    description: 'Hospital tenant settings',
    permissions: [Permissions.HOSPITAL_READ],
    icon: 'settings-2',
    section: 'admin',
    comingSoon: true,
  },
  {
    id: 'patients',
    label: 'Patients',
    href: '/app/patients',
    description: 'Patient records',
    permissions: [Permissions.PATIENT_READ],
    icon: 'heart-pulse',
    section: 'clinical',
  },
  {
    id: 'appointments',
    label: 'Appointments',
    href: '/app/appointments',
    description: 'Scheduling',
    permissions: [Permissions.APPOINTMENT_READ],
    icon: 'calendar-days',
    section: 'clinical',
  },
  {
    id: 'clinical',
    label: 'Clinical',
    href: '/app/clinical',
    description: 'Consultations and follow-ups',
    permissions: [Permissions.VISIT_READ],
    icon: 'file-plus',
    section: 'clinical',
  },
  {
    id: 'billing',
    label: 'Billing',
    href: '/app/billing',
    description: 'Invoices and payments',
    permissions: [Permissions.BILLING_READ],
    icon: 'receipt',
    section: 'admin',
    comingSoon: true,
  },
  {
    id: 'profile',
    label: 'Profile',
    href: '/app/profile',
    description: 'Your account profile',
    icon: 'user-round',
    section: 'account',
  },
];

export const NAV_SECTION_LABELS: Record<WorkspaceNavItem['section'], string> = {
  main: 'Overview',
  clinical: 'Clinical',
  admin: 'Administration',
  account: 'Account',
};

export const NAV_SECTION_ORDER: WorkspaceNavItem['section'][] = [
  'main',
  'clinical',
  'admin',
  'account',
];
