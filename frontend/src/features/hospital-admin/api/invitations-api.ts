import { apiGet, apiPost } from '@/services/http/api';
import type { InvitationStatus, RoleType } from '@/features/hospital-admin/types/enums';
import type {
  CreateInvitationPayload,
  InvitationListQuery,
  UserInvitationResponse,
} from '@/features/hospital-admin/types/invitation';
import { toPageParams } from '@/lib/page-query';
import type { PageResponse } from '@/types/api';

export type InvitationPreviewResponse = {
  email: string;
  firstName: string | null;
  lastName: string | null;
  roleType: RoleType;
  hospitalName: string;
  expiresAt: string;
  expired: boolean;
};

export type AcceptInvitationPayload = {
  token: string;
  firstName: string;
  lastName: string;
  password: string;
  phone?: string | null;
};

export type AcceptInvitationResponse = {
  invitationId: string;
  userId: string;
  tenantId: string;
  hospitalId: string;
  email: string;
  roleType: RoleType;
  message: string;
};

export type RejectInvitationPayload = {
  token: string;
};

export const invitationsApi = {
  async list(query: InvitationListQuery = {}): Promise<PageResponse<UserInvitationResponse>> {
    return apiGet<PageResponse<UserInvitationResponse>>('/invitations', {
      params: toPageParams(query),
    });
  },

  async getById(id: string): Promise<UserInvitationResponse> {
    return apiGet<UserInvitationResponse>(`/invitations/${id}`);
  },

  async create(payload: CreateInvitationPayload): Promise<UserInvitationResponse> {
    return apiPost<UserInvitationResponse>('/invitations', payload);
  },

  async resend(id: string): Promise<UserInvitationResponse> {
    return apiPost<UserInvitationResponse>(`/invitations/${id}/resend`);
  },

  async cancel(id: string): Promise<UserInvitationResponse> {
    return apiPost<UserInvitationResponse>(`/invitations/${id}/cancel`);
  },

  async preview(token: string): Promise<InvitationPreviewResponse> {
    return apiPost<InvitationPreviewResponse>('/invitations/preview', { token });
  },

  async accept(payload: AcceptInvitationPayload): Promise<AcceptInvitationResponse> {
    return apiPost<AcceptInvitationResponse>('/invitations/accept', payload);
  },

  async reject(payload: RejectInvitationPayload): Promise<InvitationPreviewResponse> {
    return apiPost<InvitationPreviewResponse>('/invitations/reject', payload);
  },
};

export type { InvitationStatus };
