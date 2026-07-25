import type { RoleType, UserManagementStatus } from './enums';
import type { ListQuery } from '@/types/api';

export type UserManagementResponse = {
  id: string;
  tenantId: string | null;
  firstName: string;
  lastName: string;
  email: string;
  phone: string | null;
  emailVerified: boolean;
  emailVerifiedAt: string | null;
  status: UserManagementStatus;
  roles: string[];
  lastLoginAt: string | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
  version: number;
};

export type AdminUpdateUserPayload = {
  firstName: string;
  lastName: string;
  phone?: string | null;
};

export type UserListQuery = ListQuery & {
  status?: UserManagementStatus;
  roleType?: RoleType;
  emailVerified?: boolean;
};

export type UserLifecycleAction = 'activate' | 'deactivate' | 'suspend' | 'restore';
