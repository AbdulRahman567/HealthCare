import { configureStore } from '@reduxjs/toolkit';

import { authorizationReducer } from '@/features/authorization/store/authorization-slice';
import { hospitalAdminUiReducer } from '@/features/hospital-admin/store/hospital-admin-ui-slice';

export const store = configureStore({
  reducer: {
    authorization: authorizationReducer,
    hospitalAdminUi: hospitalAdminUiReducer,
  },
  devTools: process.env.NODE_ENV !== 'production',
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
