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
    description: 'Workspace overview',
    permissions: [Permissions.DASHBOARD_READ],
    icon: 'layout-dashboard',
    section: 'main',
  },
  {
    id: 'profile',
    label: 'Profile',
    href: '/app/profile',
    description: 'Your account profile',
    icon: 'user-round',
    section: 'account',
  },
  {
    id: 'hospital',
    label: 'Hospital',
    href: '/app/hospital',
    description: 'Hospital tenant settings',
    permissions: [Permissions.HOSPITAL_READ],
    icon: 'building-2',
    section: 'admin',
    comingSoon: true,
  },
  {
    id: 'users',
    label: 'Users',
    href: '/app/users',
    description: 'Staff directory',
    permissions: [Permissions.USER_READ],
    icon: 'users',
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
    comingSoon: true,
  },
  {
    id: 'appointments',
    label: 'Appointments',
    href: '/app/appointments',
    description: 'Scheduling',
    permissions: [Permissions.APPOINTMENT_READ],
    icon: 'calendar-days',
    section: 'clinical',
    comingSoon: true,
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
