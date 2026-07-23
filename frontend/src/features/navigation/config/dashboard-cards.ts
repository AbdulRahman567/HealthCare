import { Permissions } from '@/features/authorization/constants/permissions';
import type { DashboardCardItem } from '@/features/navigation/types';

/**
 * Dashboard module cards. Filtered by permission before render.
 */
export const DASHBOARD_CARDS: DashboardCardItem[] = [
  {
    id: 'patients',
    title: 'Patients',
    description: 'View and manage patient records.',
    href: '/app/patients',
    permissions: [Permissions.PATIENT_READ],
    icon: 'heart-pulse',
    comingSoon: true,
  },
  {
    id: 'appointments',
    title: 'Appointments',
    description: 'Review today’s schedule and upcoming visits.',
    href: '/app/appointments',
    permissions: [Permissions.APPOINTMENT_READ],
    icon: 'calendar-days',
    comingSoon: true,
  },
  {
    id: 'users',
    title: 'Staff',
    description: 'Browse hospital users and role assignments.',
    href: '/app/users',
    permissions: [Permissions.USER_READ],
    icon: 'users',
    comingSoon: true,
  },
  {
    id: 'hospital',
    title: 'Hospital',
    description: 'Hospital profile and tenant settings.',
    href: '/app/hospital',
    permissions: [Permissions.HOSPITAL_READ],
    icon: 'building-2',
    comingSoon: true,
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
