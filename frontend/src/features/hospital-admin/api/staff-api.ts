import { apiDelete, apiGet, apiPost, apiPut } from '@/services/http/api';
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
import type { PageResponse } from '@/types/api';

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
    return apiGet<PageResponse<StaffProfile>>(`/${resourcePath(staffType)}`, {
      params: toPageParams(query),
    });
  },

  async getById(staffType: StaffType, id: string): Promise<StaffProfile> {
    return apiGet<StaffProfile>(`/${resourcePath(staffType)}/${id}`);
  },

  async create(staffType: StaffType, payload: StaffWritePayload): Promise<StaffProfile> {
    return apiPost<StaffProfile>(`/${resourcePath(staffType)}`, payload);
  },

  async update(
    staffType: StaffType,
    id: string,
    payload: StaffWritePayload,
  ): Promise<StaffProfile> {
    return apiPut<StaffProfile>(`/${resourcePath(staffType)}/${id}`, payload);
  },

  async remove(staffType: StaffType, id: string): Promise<void> {
    await apiDelete(`/${resourcePath(staffType)}/${id}`);
  },
};

export type {
  DoctorResponse,
  NurseResponse,
  ReceptionistResponse,
  LaboratoryStaffResponse,
  PharmacistResponse,
};
