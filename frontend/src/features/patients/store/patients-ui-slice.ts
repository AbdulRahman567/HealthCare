import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

type PatientsListUiState = {
  q: string;
  page: number;
  size: number;
  status: string;
  gender: string;
  bloodGroup: string;
};

const defaultListUi = (): PatientsListUiState => ({
  q: '',
  page: 0,
  size: 20,
  status: '',
  gender: '',
  bloodGroup: '',
});

type PatientsUiState = {
  list: PatientsListUiState;
  detailTab: string;
};

const initialState: PatientsUiState = {
  list: defaultListUi(),
  detailTab: 'overview',
};

const patientsUiSlice = createSlice({
  name: 'patientsUi',
  initialState,
  reducers: {
    setPatientsSearch(state, action: PayloadAction<string>) {
      state.list.q = action.payload;
      state.list.page = 0;
    },
    setPatientsStatus(state, action: PayloadAction<string>) {
      state.list.status = action.payload;
      state.list.page = 0;
    },
    setPatientsGender(state, action: PayloadAction<string>) {
      state.list.gender = action.payload;
      state.list.page = 0;
    },
    setPatientsBloodGroup(state, action: PayloadAction<string>) {
      state.list.bloodGroup = action.payload;
      state.list.page = 0;
    },
    setPatientsPage(state, action: PayloadAction<number>) {
      state.list.page = action.payload;
    },
    setPatientsSize(state, action: PayloadAction<number>) {
      state.list.size = action.payload;
      state.list.page = 0;
    },
    resetPatientsFilters(state) {
      state.list = defaultListUi();
    },
    setPatientDetailTab(state, action: PayloadAction<string>) {
      state.detailTab = action.payload;
    },
  },
});

export const {
  setPatientsSearch,
  setPatientsStatus,
  setPatientsGender,
  setPatientsBloodGroup,
  setPatientsPage,
  setPatientsSize,
  resetPatientsFilters,
  setPatientDetailTab,
} = patientsUiSlice.actions;

export const patientsUiReducer = patientsUiSlice.reducer;

export const selectPatientsListUi = (state: { patientsUi: PatientsUiState }) => state.patientsUi.list;
export const selectPatientDetailTab = (state: { patientsUi: PatientsUiState }) =>
  state.patientsUi.detailTab;
