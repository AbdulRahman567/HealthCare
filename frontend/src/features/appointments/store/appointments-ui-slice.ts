import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

import type { CalendarScope, CalendarViewType } from '@/features/appointments/types/enums';

type AppointmentsListUiState = {
  appointmentNumber: string;
  patientName: string;
  doctorName: string;
  departmentName: string;
  status: string;
  visitType: string;
  queueStatus: string;
  fromDate: string;
  toDate: string;
  page: number;
  size: number;
};

type CalendarUiState = {
  scope: CalendarScope;
  scopeId: string;
  view: CalendarViewType;
  date: string;
  year: number;
  month: number;
  status: string;
};

type QueueUiState = {
  doctorId: string;
  date: string;
};

const todayIso = () => new Date().toISOString().slice(0, 10);

const defaultListUi = (): AppointmentsListUiState => ({
  appointmentNumber: '',
  patientName: '',
  doctorName: '',
  departmentName: '',
  status: '',
  visitType: '',
  queueStatus: '',
  fromDate: '',
  toDate: '',
  page: 0,
  size: 20,
});

const defaultCalendarUi = (): CalendarUiState => {
  const now = new Date();
  return {
    scope: 'DOCTOR',
    scopeId: '',
    view: 'DAILY',
    date: todayIso(),
    year: now.getFullYear(),
    month: now.getMonth() + 1,
    status: '',
  };
};

const defaultQueueUi = (): QueueUiState => ({
  doctorId: '',
  date: todayIso(),
});

type AppointmentsUiState = {
  list: AppointmentsListUiState;
  calendar: CalendarUiState;
  queue: QueueUiState;
};

const initialState: AppointmentsUiState = {
  list: defaultListUi(),
  calendar: defaultCalendarUi(),
  queue: defaultQueueUi(),
};

const appointmentsUiSlice = createSlice({
  name: 'appointmentsUi',
  initialState,
  reducers: {
    setAppointmentsAppointmentNumber(state, action: PayloadAction<string>) {
      state.list.appointmentNumber = action.payload;
      state.list.page = 0;
    },
    setAppointmentsPatientName(state, action: PayloadAction<string>) {
      state.list.patientName = action.payload;
      state.list.page = 0;
    },
    setAppointmentsDoctorName(state, action: PayloadAction<string>) {
      state.list.doctorName = action.payload;
      state.list.page = 0;
    },
    setAppointmentsDepartmentName(state, action: PayloadAction<string>) {
      state.list.departmentName = action.payload;
      state.list.page = 0;
    },
    setAppointmentsStatus(state, action: PayloadAction<string>) {
      state.list.status = action.payload;
      state.list.page = 0;
    },
    setAppointmentsVisitType(state, action: PayloadAction<string>) {
      state.list.visitType = action.payload;
      state.list.page = 0;
    },
    setAppointmentsQueueStatus(state, action: PayloadAction<string>) {
      state.list.queueStatus = action.payload;
      state.list.page = 0;
    },
    setAppointmentsFromDate(state, action: PayloadAction<string>) {
      state.list.fromDate = action.payload;
      state.list.page = 0;
    },
    setAppointmentsToDate(state, action: PayloadAction<string>) {
      state.list.toDate = action.payload;
      state.list.page = 0;
    },
    setAppointmentsPage(state, action: PayloadAction<number>) {
      state.list.page = action.payload;
    },
    setAppointmentsSize(state, action: PayloadAction<number>) {
      state.list.size = action.payload;
      state.list.page = 0;
    },
    resetAppointmentsFilters(state) {
      state.list = defaultListUi();
    },
    setCalendarScope(state, action: PayloadAction<CalendarScope>) {
      state.calendar.scope = action.payload;
      state.calendar.scopeId = '';
    },
    setCalendarScopeId(state, action: PayloadAction<string>) {
      state.calendar.scopeId = action.payload;
    },
    setCalendarView(state, action: PayloadAction<CalendarViewType>) {
      state.calendar.view = action.payload;
    },
    setCalendarDate(state, action: PayloadAction<string>) {
      state.calendar.date = action.payload;
      if (action.payload) {
        const parsed = new Date(`${action.payload}T00:00:00`);
        if (!Number.isNaN(parsed.getTime())) {
          state.calendar.year = parsed.getFullYear();
          state.calendar.month = parsed.getMonth() + 1;
        }
      }
    },
    setCalendarYearMonth(state, action: PayloadAction<{ year: number; month: number }>) {
      state.calendar.year = action.payload.year;
      state.calendar.month = action.payload.month;
    },
    setCalendarStatus(state, action: PayloadAction<string>) {
      state.calendar.status = action.payload;
    },
    setQueueDoctorId(state, action: PayloadAction<string>) {
      state.queue.doctorId = action.payload;
    },
    setQueueDate(state, action: PayloadAction<string>) {
      state.queue.date = action.payload;
    },
  },
});

export const {
  setAppointmentsAppointmentNumber,
  setAppointmentsPatientName,
  setAppointmentsDoctorName,
  setAppointmentsDepartmentName,
  setAppointmentsStatus,
  setAppointmentsVisitType,
  setAppointmentsQueueStatus,
  setAppointmentsFromDate,
  setAppointmentsToDate,
  setAppointmentsPage,
  setAppointmentsSize,
  resetAppointmentsFilters,
  setCalendarScope,
  setCalendarScopeId,
  setCalendarView,
  setCalendarDate,
  setCalendarYearMonth,
  setCalendarStatus,
  setQueueDoctorId,
  setQueueDate,
} = appointmentsUiSlice.actions;

export const appointmentsUiReducer = appointmentsUiSlice.reducer;

export const selectAppointmentsListUi = (state: { appointmentsUi: AppointmentsUiState }) =>
  state.appointmentsUi.list;
export const selectAppointmentsCalendarUi = (state: { appointmentsUi: AppointmentsUiState }) =>
  state.appointmentsUi.calendar;
export const selectAppointmentsQueueUi = (state: { appointmentsUi: AppointmentsUiState }) =>
  state.appointmentsUi.queue;
