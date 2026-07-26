import { configureStore } from '@reduxjs/toolkit';

import { authorizationReducer } from '@/features/authorization/store/authorization-slice';
import { hospitalAdminUiReducer } from '@/features/hospital-admin/store/hospital-admin-ui-slice';
import { patientsUiReducer } from '@/features/patients/store/patients-ui-slice';

export const store = configureStore({
  reducer: {
    authorization: authorizationReducer,
    hospitalAdminUi: hospitalAdminUiReducer,
    patientsUi: patientsUiReducer,
  },
  devTools: process.env.NODE_ENV !== 'production',
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
