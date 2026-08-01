import { apiDelete, apiGet, apiPost, apiPut } from '@/services/http/api';
import type {
  DepartmentListQuery,
  DepartmentResponse,
  DepartmentWritePayload,
} from '@/features/hospital-admin/types/department';
import { toPageParams } from '@/lib/page-query';
import type { PageResponse } from '@/types/api';

export const departmentsApi = {
  async list(query: DepartmentListQuery = {}): Promise<PageResponse<DepartmentResponse>> {
    return apiGet<PageResponse<DepartmentResponse>>('/departments', {
      params: toPageParams(query),
    });
  },

  async getById(id: string): Promise<DepartmentResponse> {
    return apiGet<DepartmentResponse>(`/departments/${id}`);
  },

  async create(payload: DepartmentWritePayload): Promise<DepartmentResponse> {
    return apiPost<DepartmentResponse>('/departments', payload);
  },

  async update(id: string, payload: DepartmentWritePayload): Promise<DepartmentResponse> {
    return apiPut<DepartmentResponse>(`/departments/${id}`, payload);
  },

  async remove(id: string): Promise<void> {
    await apiDelete(`/departments/${id}`);
  },
};
