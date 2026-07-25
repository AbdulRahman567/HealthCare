import type { InvitationStatus, RoleType } from './enums';
import type { ListQuery } from '@/types/api';

export type UserInvitationResponse = {
  id: string;
  tenantId: string;
  hospitalId: string;
  email: string;
  firstName: string | null;
  lastName: string | null;
  roleType: RoleType;
  invitedBy: string;
  status: InvitationStatus;
  expiresAt: string;
  acceptedAt: string | null;
  rejectedAt: string | null;
  cancelledAt: string | null;
  acceptedUserId: string | null;
  message: string | null;
  expired: boolean;
  createdAt: string;
  updatedAt: string;
  version: number;
};

export type CreateInvitationPayload = {
  email: string;
  firstName?: string | null;
  lastName?: string | null;
  roleType: RoleType;
  message?: string | null;
};

export type InvitationListQuery = ListQuery & {
  status?: InvitationStatus;
  email?: string;
};
