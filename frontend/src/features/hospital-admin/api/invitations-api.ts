import type { InvitationStatus, RoleType } from '@/features/hospital-admin/types/enums';
import type {
  CreateInvitationPayload,
  InvitationListQuery,
  UserInvitationResponse,
} from '@/features/hospital-admin/types/invitation';
import { toPageParams } from '@/lib/page-query';
import { apiClient } from '@/services/http/api-client';
import type { ApiSuccessResponse, PageResponse } from '@/types/api';

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
    const { data } = await apiClient.get<ApiSuccessResponse<PageResponse<UserInvitationResponse>>>(
      '/invitations',
      { params: toPageParams(query) },
    );
    return data.data;
  },

  async getById(id: string): Promise<UserInvitationResponse> {
    const { data } = await apiClient.get<ApiSuccessResponse<UserInvitationResponse>>(
      `/invitations/${id}`,
    );
    return data.data;
  },

  async create(payload: CreateInvitationPayload): Promise<UserInvitationResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<UserInvitationResponse>>(
      '/invitations',
      payload,
    );
    return data.data;
  },

  async resend(id: string): Promise<UserInvitationResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<UserInvitationResponse>>(
      `/invitations/${id}/resend`,
    );
    return data.data;
  },

  async cancel(id: string): Promise<UserInvitationResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<UserInvitationResponse>>(
      `/invitations/${id}/cancel`,
    );
    return data.data;
  },

  async preview(token: string): Promise<InvitationPreviewResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<InvitationPreviewResponse>>(
      '/invitations/preview',
      { token },
    );
    return data.data;
  },

  async accept(payload: AcceptInvitationPayload): Promise<AcceptInvitationResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<AcceptInvitationResponse>>(
      '/invitations/accept',
      payload,
    );
    return data.data;
  },

  async reject(payload: RejectInvitationPayload): Promise<InvitationPreviewResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<InvitationPreviewResponse>>(
      '/invitations/reject',
      payload,
    );
    return data.data;
  },
};

export type { InvitationStatus };
