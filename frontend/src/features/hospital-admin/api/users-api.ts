import { apiClient } from '@/services/http/api-client';
import type {
  AdminUpdateUserPayload,
  UserLifecycleAction,
  UserListQuery,
  UserManagementResponse,
} from '@/features/hospital-admin/types/user';
import { toPageParams } from '@/lib/page-query';
import type { ApiSuccessResponse, PageResponse } from '@/types/api';

export const usersApi = {
  async list(query: UserListQuery = {}): Promise<PageResponse<UserManagementResponse>> {
    const { data } = await apiClient.get<ApiSuccessResponse<PageResponse<UserManagementResponse>>>(
      '/users',
      { params: toPageParams(query) },
    );
    return data.data;
  },

  async getById(id: string): Promise<UserManagementResponse> {
    const { data } = await apiClient.get<ApiSuccessResponse<UserManagementResponse>>(
      `/users/${id}`,
    );
    return data.data;
  },

  async update(id: string, payload: AdminUpdateUserPayload): Promise<UserManagementResponse> {
    const { data } = await apiClient.put<ApiSuccessResponse<UserManagementResponse>>(
      `/users/${id}`,
      payload,
    );
    return data.data;
  },

  async lifecycle(id: string, action: UserLifecycleAction): Promise<UserManagementResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<UserManagementResponse>>(
      `/users/${id}/${action}`,
    );
    return data.data;
  },
};
