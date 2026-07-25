export const DEPARTMENT_TYPES = [
  'CLINICAL',
  'DIAGNOSTIC',
  'EMERGENCY',
  'ADMINISTRATIVE',
  'SUPPORT',
  'RESEARCH',
  'OTHER',
] as const;

export type DepartmentType = (typeof DEPARTMENT_TYPES)[number];

export const DEPARTMENT_STATUSES = ['ACTIVE', 'INACTIVE', 'SUSPENDED'] as const;
export type DepartmentStatus = (typeof DEPARTMENT_STATUSES)[number];

export const EMPLOYMENT_STATUSES = [
  'PENDING',
  'ACTIVE',
  'ON_LEAVE',
  'SUSPENDED',
  'TERMINATED',
] as const;
export type EmploymentStatus = (typeof EMPLOYMENT_STATUSES)[number];

export const EMPLOYMENT_TYPES = [
  'FULL_TIME',
  'PART_TIME',
  'CONTRACT',
  'TEMPORARY',
  'INTERN',
  'CONSULTANT',
] as const;
export type EmploymentType = (typeof EMPLOYMENT_TYPES)[number];

export const STAFF_TYPES = [
  'DOCTOR',
  'NURSE',
  'RECEPTIONIST',
  'LABORATORY_STAFF',
  'PHARMACIST',
] as const;
export type StaffType = (typeof STAFF_TYPES)[number];

export const STAFF_SHIFTS = ['MORNING', 'EVENING', 'NIGHT', 'ROTATING'] as const;
export type StaffShift = (typeof STAFF_SHIFTS)[number];

export const USER_STATUSES = ['ACTIVE', 'INACTIVE', 'SUSPENDED', 'LOCKED', 'PENDING'] as const;
export type UserManagementStatus = (typeof USER_STATUSES)[number];

export const INVITATION_STATUSES = [
  'PENDING',
  'ACCEPTED',
  'REJECTED',
  'CANCELLED',
  'EXPIRED',
] as const;
export type InvitationStatus = (typeof INVITATION_STATUSES)[number];

export const ROLE_TYPES = [
  'SUPER_ADMIN',
  'HOSPITAL_ADMIN',
  'DOCTOR',
  'NURSE',
  'RECEPTIONIST',
  'LAB_TECHNICIAN',
  'PHARMACIST',
  'ACCOUNTANT',
  'PATIENT',
] as const;
export type RoleType = (typeof ROLE_TYPES)[number];

/** Roles that hospital admins typically invite (excludes platform-only). */
export const INVITABLE_ROLE_TYPES = [
  'DOCTOR',
  'NURSE',
  'RECEPTIONIST',
  'LAB_TECHNICIAN',
  'PHARMACIST',
  'ACCOUNTANT',
] as const satisfies readonly RoleType[];
