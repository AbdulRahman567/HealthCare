import type { DepartmentStatus, DepartmentType, StaffType } from './enums';
import type { ListQuery } from '@/types/api';

export type DepartmentResponse = {
  id: string;
  tenantId: string;
  hospitalId: string;
  name: string;
  code: string;
  description: string | null;
  departmentType: DepartmentType;
  status: DepartmentStatus;
  location: string | null;
  headUserId: string | null;
  headStaffId: string | null;
  headStaffType: StaffType | null;
  createdAt: string;
  updatedAt: string;
  createdBy: string | null;
  updatedBy: string | null;
  version: number;
};

export type DepartmentWritePayload = {
  name: string;
  code: string;
  description?: string | null;
  departmentType: DepartmentType;
  status: DepartmentStatus;
  location?: string | null;
  headUserId?: string | null;
};

export type DepartmentListQuery = ListQuery & {
  status?: DepartmentStatus;
  type?: DepartmentType;
  hospitalId?: string;
};
