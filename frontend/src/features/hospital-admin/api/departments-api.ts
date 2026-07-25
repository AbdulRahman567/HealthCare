import { apiClient } from '@/services/http/api-client';
import type {
  DepartmentListQuery,
  DepartmentResponse,
  DepartmentWritePayload,
} from '@/features/hospital-admin/types/department';
import { toPageParams } from '@/lib/page-query';
import type { ApiSuccessResponse, PageResponse } from '@/types/api';

export const departmentsApi = {
  async list(query: DepartmentListQuery = {}): Promise<PageResponse<DepartmentResponse>> {
    const { data } = await apiClient.get<ApiSuccessResponse<PageResponse<DepartmentResponse>>>(
      '/departments',
      { params: toPageParams(query) },
    );
    return data.data;
  },

  async getById(id: string): Promise<DepartmentResponse> {
    const { data } = await apiClient.get<ApiSuccessResponse<DepartmentResponse>>(
      `/departments/${id}`,
    );
    return data.data;
  },

  async create(payload: DepartmentWritePayload): Promise<DepartmentResponse> {
    const { data } = await apiClient.post<ApiSuccessResponse<DepartmentResponse>>(
      '/departments',
      payload,
    );
    return data.data;
  },

  async update(id: string, payload: DepartmentWritePayload): Promise<DepartmentResponse> {
    const { data } = await apiClient.put<ApiSuccessResponse<DepartmentResponse>>(
      `/departments/${id}`,
      payload,
    );
    return data.data;
  },

  async remove(id: string): Promise<void> {
    await apiClient.delete(`/departments/${id}`);
  },
};
