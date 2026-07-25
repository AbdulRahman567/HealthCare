import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

import type { StaffType } from '@/features/hospital-admin/types/enums';

export type ListUiState = {
  q: string;
  page: number;
  size: number;
  status: string;
};

const defaultListUi = (): ListUiState => ({
  q: '',
  page: 0,
  size: 20,
  status: '',
});

type HospitalAdminUiState = {
  departments: ListUiState;
  staff: ListUiState & { staffType: StaffType; departmentId: string };
  users: ListUiState & { roleType: string; emailVerified: string };
  invitations: ListUiState & { email: string };
};

const initialState: HospitalAdminUiState = {
  departments: defaultListUi(),
  staff: { ...defaultListUi(), staffType: 'DOCTOR', departmentId: '' },
  users: { ...defaultListUi(), roleType: '', emailVerified: '' },
  invitations: { ...defaultListUi(), email: '' },
};

type ListKey = keyof HospitalAdminUiState;

const hospitalAdminUiSlice = createSlice({
  name: 'hospitalAdminUi',
  initialState,
  reducers: {
    setListSearch(state, action: PayloadAction<{ key: ListKey; q: string }>) {
      const list = state[action.payload.key];
      list.q = action.payload.q;
      list.page = 0;
    },
    setListStatus(state, action: PayloadAction<{ key: ListKey; status: string }>) {
      const list = state[action.payload.key];
      list.status = action.payload.status;
      list.page = 0;
    },
    setListPage(state, action: PayloadAction<{ key: ListKey; page: number }>) {
      state[action.payload.key].page = action.payload.page;
    },
    setListSize(state, action: PayloadAction<{ key: ListKey; size: number }>) {
      const list = state[action.payload.key];
      list.size = action.payload.size;
      list.page = 0;
    },
    setStaffType(state, action: PayloadAction<StaffType>) {
      state.staff.staffType = action.payload;
      state.staff.page = 0;
    },
    setStaffDepartmentId(state, action: PayloadAction<string>) {
      state.staff.departmentId = action.payload;
      state.staff.page = 0;
    },
    setUsersRoleType(state, action: PayloadAction<string>) {
      state.users.roleType = action.payload;
      state.users.page = 0;
    },
    setUsersEmailVerified(state, action: PayloadAction<string>) {
      state.users.emailVerified = action.payload;
      state.users.page = 0;
    },
    setInvitationsEmail(state, action: PayloadAction<string>) {
      state.invitations.email = action.payload;
      state.invitations.page = 0;
    },
    resetListFilters(state, action: PayloadAction<ListKey>) {
      if (action.payload === 'staff') {
        state.staff = { ...defaultListUi(), staffType: state.staff.staffType, departmentId: '' };
        return;
      }
      if (action.payload === 'users') {
        state.users = { ...defaultListUi(), roleType: '', emailVerified: '' };
        return;
      }
      if (action.payload === 'invitations') {
        state.invitations = { ...defaultListUi(), email: '' };
        return;
      }
      state.departments = defaultListUi();
    },
  },
});

export const {
  setListSearch,
  setListStatus,
  setListPage,
  setListSize,
  setStaffType,
  setStaffDepartmentId,
  setUsersRoleType,
  setUsersEmailVerified,
  setInvitationsEmail,
  resetListFilters,
} = hospitalAdminUiSlice.actions;

export const hospitalAdminUiReducer = hospitalAdminUiSlice.reducer;

export const selectDepartmentsUi = (state: { hospitalAdminUi: HospitalAdminUiState }) =>
  state.hospitalAdminUi.departments;
export const selectStaffUi = (state: { hospitalAdminUi: HospitalAdminUiState }) =>
  state.hospitalAdminUi.staff;
export const selectUsersUi = (state: { hospitalAdminUi: HospitalAdminUiState }) =>
  state.hospitalAdminUi.users;
export const selectInvitationsUi = (state: { hospitalAdminUi: HospitalAdminUiState }) =>
  state.hospitalAdminUi.invitations;
