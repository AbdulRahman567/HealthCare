import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

import type { AuthorizationContext, AuthorizationStatus } from '@/features/authorization/types';

export type AuthorizationState = {
  status: AuthorizationStatus;
  userId: string | null;
  tenantId: string | null;
  email: string | null;
  roles: string[];
  permissions: string[];
  error: string | null;
};

const initialState: AuthorizationState = {
  status: 'idle',
  userId: null,
  tenantId: null,
  email: null,
  roles: [],
  permissions: [],
  error: null,
};

type HydratePayload = {
  userId: string;
  tenantId: string | null;
  email?: string | null;
  roles: string[];
  permissions: string[];
};

const authorizationSlice = createSlice({
  name: 'authorization',
  initialState,
  reducers: {
    authorizationLoading(state) {
      state.status = 'loading';
      state.error = null;
    },
    authorizationHydrated(state, action: PayloadAction<HydratePayload>) {
      state.status = 'ready';
      state.userId = action.payload.userId;
      state.tenantId = action.payload.tenantId;
      state.email = action.payload.email ?? null;
      state.roles = action.payload.roles;
      state.permissions = action.payload.permissions;
      state.error = null;
    },
    authorizationFromContext(state, action: PayloadAction<AuthorizationContext>) {
      state.status = 'ready';
      state.userId = action.payload.userId;
      state.tenantId = action.payload.tenantId;
      state.email = action.payload.email;
      state.roles = action.payload.roles;
      state.permissions = action.payload.permissions;
      state.error = null;
    },
    authorizationFailed(state, action: PayloadAction<string>) {
      // Keep profile-hydrated grants; only mark error when never ready.
      if (state.status !== 'ready') {
        state.status = 'error';
      }
      state.error = action.payload;
    },
    authorizationCleared() {
      return initialState;
    },
  },
});

export const {
  authorizationLoading,
  authorizationHydrated,
  authorizationFromContext,
  authorizationFailed,
  authorizationCleared,
} = authorizationSlice.actions;

export const authorizationReducer = authorizationSlice.reducer;

export const selectAuthorization = (state: { authorization: AuthorizationState }) =>
  state.authorization;
export const selectRoles = (state: { authorization: AuthorizationState }) =>
  state.authorization.roles;
export const selectPermissions = (state: { authorization: AuthorizationState }) =>
  state.authorization.permissions;
