import { Permissions } from '@/features/authorization/constants/permissions';
import type { DashboardCardItem } from '@/features/navigation/types';

/**
 * Dashboard module cards. Filtered by permission before render.
 */
export const DASHBOARD_CARDS: DashboardCardItem[] = [
  {
    id: 'departments',
    title: 'Departments',
    description: 'Create and manage hospital departments.',
    href: '/app/departments',
    permissions: [Permissions.DEPARTMENT_READ],
    icon: 'building-2',
  },
  {
    id: 'staff',
    title: 'Staff directory',
    description: 'Employment profiles for clinical and support staff.',
    href: '/app/staff',
    permissions: [Permissions.STAFF_READ, Permissions.DOCTOR_READ],
    mode: 'any',
    icon: 'stethoscope',
  },
  {
    id: 'users',
    title: 'Users',
    description: 'Browse accounts and manage user status.',
    href: '/app/users',
    permissions: [Permissions.USER_READ],
    icon: 'users',
  },
  {
    id: 'invitations',
    title: 'Invitations',
    description: 'Invite staff and track acceptance status.',
    href: '/app/invitations',
    permissions: [Permissions.USER_READ],
    icon: 'mail-plus',
  },
  {
    id: 'hospital',
    title: 'Hospital settings',
    description: 'Hospital profile and tenant settings.',
    href: '/app/hospital',
    permissions: [Permissions.HOSPITAL_READ],
    icon: 'settings-2',
    comingSoon: true,
  },
  {
    id: 'patients',
    title: 'Patients',
    description: 'View and manage patient records.',
    href: '/app/patients',
    permissions: [Permissions.PATIENT_READ],
    icon: 'heart-pulse',
  },
  {
    id: 'appointments',
    title: 'Appointments',
    description: 'Review today’s schedule and upcoming visits.',
    href: '/app/appointments',
    permissions: [Permissions.APPOINTMENT_READ],
    icon: 'calendar-days',
  },
  {
    id: 'billing',
    title: 'Billing',
    description: 'Invoices, payments, and billing queues.',
    href: '/app/billing',
    permissions: [Permissions.BILLING_READ],
    icon: 'receipt',
    comingSoon: true,
  },
  {
    id: 'profile',
    title: 'Your profile',
    description: 'Update personal account details.',
    href: '/app/profile',
    icon: 'user-round',
  },
];
