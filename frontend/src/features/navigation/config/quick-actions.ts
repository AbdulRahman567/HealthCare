import { Permissions } from '@/features/authorization/constants/permissions';
import type { QuickActionItem } from '@/features/navigation/types';

/**
 * Permission-gated quick actions shown on the dashboard and top nav shortcuts.
 * Actions require the matching create/update permission where applicable.
 */
export const QUICK_ACTIONS: QuickActionItem[] = [
  {
    id: 'invite-user',
    label: 'Invite staff',
    description: 'Send a hospital invitation email',
    href: '/app/invitations',
    permissions: [Permissions.USER_CREATE],
    icon: 'mail-plus',
  },
  {
    id: 'add-department',
    label: 'Add department',
    description: 'Create an organizational unit',
    href: '/app/departments',
    permissions: [Permissions.DEPARTMENT_CREATE],
    icon: 'building-2',
  },
  {
    id: 'add-staff',
    label: 'Add staff profile',
    description: 'Link a user to employment records',
    href: '/app/staff',
    permissions: [Permissions.STAFF_CREATE, Permissions.DOCTOR_CREATE],
    mode: 'any',
    icon: 'stethoscope',
  },
  {
    id: 'manage-users',
    label: 'Manage users',
    description: 'Search and update hospital accounts',
    href: '/app/users',
    permissions: [Permissions.USER_READ],
    icon: 'users',
  },
  {
    id: 'register-patient',
    label: 'Register patient',
    description: 'Create a new patient record',
    href: '/app/patients/new',
    permissions: [Permissions.PATIENT_CREATE],
    icon: 'user-plus',
  },
  {
    id: 'book-appointment',
    label: 'Book appointment',
    description: 'Schedule a patient visit',
    href: '/app/appointments',
    permissions: [Permissions.APPOINTMENT_CREATE],
    icon: 'calendar-plus',
    comingSoon: true,
  },
  {
    id: 'edit-hospital',
    label: 'Edit hospital settings',
    description: 'Update hospital configuration',
    href: '/app/hospital',
    permissions: [Permissions.HOSPITAL_UPDATE],
    icon: 'settings-2',
    comingSoon: true,
  },
  {
    id: 'create-invoice',
    label: 'Create invoice',
    description: 'Start a billing document',
    href: '/app/billing',
    permissions: [Permissions.BILLING_CREATE],
    icon: 'file-plus',
    comingSoon: true,
  },
];
