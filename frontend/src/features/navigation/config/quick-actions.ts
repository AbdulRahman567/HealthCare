import { Permissions } from '@/features/authorization/constants/permissions';
import type { QuickActionItem } from '@/features/navigation/types';

/**
 * Permission-gated quick actions shown on the dashboard and top nav shortcuts.
 * Actions require the matching create/update permission where applicable.
 */
export const QUICK_ACTIONS: QuickActionItem[] = [
  {
    id: 'register-patient',
    label: 'Register patient',
    description: 'Create a new patient record',
    href: '/app/patients',
    permissions: [Permissions.PATIENT_CREATE],
    icon: 'user-plus',
    comingSoon: true,
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
    id: 'invite-user',
    label: 'Invite staff',
    description: 'Add a hospital user',
    href: '/app/users',
    permissions: [Permissions.USER_CREATE],
    icon: 'users',
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
