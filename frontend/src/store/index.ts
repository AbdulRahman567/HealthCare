import { configureStore } from '@reduxjs/toolkit';

import { authorizationReducer } from '@/features/authorization/store/authorization-slice';

export const store = configureStore({
  reducer: {
    authorization: authorizationReducer,
  },
  devTools: process.env.NODE_ENV !== 'production',
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
