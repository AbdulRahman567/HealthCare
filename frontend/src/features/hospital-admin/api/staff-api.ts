import { apiClient } from '@/services/http/api-client';
import type {
  CreateDoctorPayload,
  CreateLaboratoryStaffPayload,
  CreateNursePayload,
  CreatePharmacistPayload,
  CreateReceptionistPayload,
  DoctorResponse,
  LaboratoryStaffResponse,
  NurseResponse,
  PharmacistResponse,
  ReceptionistResponse,
  StaffListQuery,
  StaffProfile,
  StaffResourceKey,
} from '@/features/hospital-admin/types/staff';
import { STAFF_TYPE_TO_RESOURCE } from '@/features/hospital-admin/types/staff';
import type { StaffType } from '@/features/hospital-admin/types/enums';
import { toPageParams } from '@/lib/page-query';
import type { ApiSuccessResponse, PageResponse } from '@/types/api';

type StaffWritePayload =
  | CreateDoctorPayload
  | CreateNursePayload
  | CreateReceptionistPayload
  | CreateLaboratoryStaffPayload
  | CreatePharmacistPayload;

function resourcePath(staffType: StaffType): StaffResourceKey {
  return STAFF_TYPE_TO_RESOURCE[staffType];
}

export const staffApi = {
  async list(
    staffType: StaffType,
    query: StaffListQuery = {},
  ): Promise<PageResponse<StaffProfile>> {
    const { data } = await apiClient.get<ApiSuccessResponse<PageResponse<StaffProfile>>>(
      `/${resourcePath(staffType)}`,
      { params: toPageParams(query) },
    );
    return data.data;
  },

  async getById(staffType: StaffType, id: string): Promise<StaffProfile> {
    const { data } = await apiClient.get<ApiSuccessResponse<StaffProfile>>(
      `/${resourcePath(staffType)}/${id}`,
    );
    return data.data;
  },

  async create(staffType: StaffType, payload: StaffWritePayload): Promise<StaffProfile> {
    const { data } = await apiClient.post<ApiSuccessResponse<StaffProfile>>(
      `/${resourcePath(staffType)}`,
      payload,
    );
    return data.data;
  },

  async update(
    staffType: StaffType,
    id: string,
    payload: StaffWritePayload,
  ): Promise<StaffProfile> {
    const { data } = await apiClient.put<ApiSuccessResponse<StaffProfile>>(
      `/${resourcePath(staffType)}/${id}`,
      payload,
    );
    return data.data;
  },

  async remove(staffType: StaffType, id: string): Promise<void> {
    await apiClient.delete(`/${resourcePath(staffType)}/${id}`);
  },
};

export type {
  DoctorResponse,
  NurseResponse,
  ReceptionistResponse,
  LaboratoryStaffResponse,
  PharmacistResponse,
};
