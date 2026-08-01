import { apiGet, apiPost, apiPut } from '@/services/http/api';
import type {
  AdminUpdateUserPayload,
  UserLifecycleAction,
  UserListQuery,
  UserManagementResponse,
} from '@/features/hospital-admin/types/user';
import { toPageParams } from '@/lib/page-query';
import type { PageResponse } from '@/types/api';

export const usersApi = {
  async list(query: UserListQuery = {}): Promise<PageResponse<UserManagementResponse>> {
    return apiGet<PageResponse<UserManagementResponse>>('/users', {
      params: toPageParams(query),
    });
  },

  async getById(id: string): Promise<UserManagementResponse> {
    return apiGet<UserManagementResponse>(`/users/${id}`);
  },

  async update(id: string, payload: AdminUpdateUserPayload): Promise<UserManagementResponse> {
    return apiPut<UserManagementResponse>(`/users/${id}`, payload);
  },

  async lifecycle(id: string, action: UserLifecycleAction): Promise<UserManagementResponse> {
    return apiPost<UserManagementResponse>(`/users/${id}/${action}`);
  },
};
