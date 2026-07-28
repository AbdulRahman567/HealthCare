import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

import type { ConsultationWorkspaceTab } from '@/features/clinical/types/enums';

type ClinicalListUiState = {
  consultationNumber: string;
  patientName: string;
  doctorName: string;
  status: string;
  fromDate: string;
  toDate: string;
  page: number;
  size: number;
};

type FollowUpWorklistUiState = {
  withinDays: number;
  status: string;
  priority: string;
  overdueOnly: boolean;
  page: number;
  size: number;
};

type ClinicalUiState = {
  list: ClinicalListUiState;
  workspaceTab: ConsultationWorkspaceTab;
  followUps: FollowUpWorklistUiState;
};

const defaultListUi = (): ClinicalListUiState => ({
  consultationNumber: '',
  patientName: '',
  doctorName: '',
  status: '',
  fromDate: '',
  toDate: '',
  page: 0,
  size: 20,
});

const defaultFollowUpUi = (): FollowUpWorklistUiState => ({
  withinDays: 14,
  status: '',
  priority: '',
  overdueOnly: false,
  page: 0,
  size: 20,
});

const initialState: ClinicalUiState = {
  list: defaultListUi(),
  workspaceTab: 'chart',
  followUps: defaultFollowUpUi(),
};

const clinicalUiSlice = createSlice({
  name: 'clinicalUi',
  initialState,
  reducers: {
    setClinicalConsultationNumber(state, action: PayloadAction<string>) {
      state.list.consultationNumber = action.payload;
      state.list.page = 0;
    },
    setClinicalPatientName(state, action: PayloadAction<string>) {
      state.list.patientName = action.payload;
      state.list.page = 0;
    },
    setClinicalDoctorName(state, action: PayloadAction<string>) {
      state.list.doctorName = action.payload;
      state.list.page = 0;
    },
    setClinicalStatus(state, action: PayloadAction<string>) {
      state.list.status = action.payload;
      state.list.page = 0;
    },
    setClinicalFromDate(state, action: PayloadAction<string>) {
      state.list.fromDate = action.payload;
      state.list.page = 0;
    },
    setClinicalToDate(state, action: PayloadAction<string>) {
      state.list.toDate = action.payload;
      state.list.page = 0;
    },
    setClinicalPage(state, action: PayloadAction<number>) {
      state.list.page = action.payload;
    },
    setClinicalSize(state, action: PayloadAction<number>) {
      state.list.size = action.payload;
      state.list.page = 0;
    },
    resetClinicalFilters(state) {
      state.list = defaultListUi();
    },
    setClinicalWorkspaceTab(state, action: PayloadAction<ConsultationWorkspaceTab>) {
      state.workspaceTab = action.payload;
    },
    setFollowUpWithinDays(state, action: PayloadAction<number>) {
      state.followUps.withinDays = action.payload;
      state.followUps.page = 0;
    },
    setFollowUpStatusFilter(state, action: PayloadAction<string>) {
      state.followUps.status = action.payload;
      state.followUps.page = 0;
    },
    setFollowUpPriorityFilter(state, action: PayloadAction<string>) {
      state.followUps.priority = action.payload;
      state.followUps.page = 0;
    },
    setFollowUpOverdueOnly(state, action: PayloadAction<boolean>) {
      state.followUps.overdueOnly = action.payload;
      state.followUps.page = 0;
    },
    setFollowUpPage(state, action: PayloadAction<number>) {
      state.followUps.page = action.payload;
    },
    resetFollowUpFilters(state) {
      state.followUps = defaultFollowUpUi();
    },
  },
});

export const {
  setClinicalConsultationNumber,
  setClinicalPatientName,
  setClinicalDoctorName,
  setClinicalStatus,
  setClinicalFromDate,
  setClinicalToDate,
  setClinicalPage,
  setClinicalSize,
  resetClinicalFilters,
  setClinicalWorkspaceTab,
  setFollowUpWithinDays,
  setFollowUpStatusFilter,
  setFollowUpPriorityFilter,
  setFollowUpOverdueOnly,
  setFollowUpPage,
  resetFollowUpFilters,
} = clinicalUiSlice.actions;

export const clinicalUiReducer = clinicalUiSlice.reducer;

export const selectClinicalListUi = (state: { clinicalUi: ClinicalUiState }) =>
  state.clinicalUi.list;
export const selectClinicalWorkspaceTab = (state: { clinicalUi: ClinicalUiState }) =>
  state.clinicalUi.workspaceTab;
export const selectFollowUpWorklistUi = (state: { clinicalUi: ClinicalUiState }) =>
  state.clinicalUi.followUps;
