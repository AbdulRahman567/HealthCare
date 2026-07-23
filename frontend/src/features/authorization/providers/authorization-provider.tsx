'use client';

import { useEffect, type ReactNode } from 'react';

import { useAuthorizationQuery } from '@/features/authorization/hooks/use-authorization-query';
import { PermissionProvider } from '@/features/authorization/providers/permission-provider';
import { RoleProvider } from '@/features/authorization/providers/role-provider';
import {
  authorizationCleared,
  authorizationFailed,
  authorizationFromContext,
  authorizationHydrated,
  authorizationLoading,
} from '@/features/authorization/store/authorization-slice';
import { useSession } from '@/providers/session-provider';
import { useAppDispatch } from '@/store/hooks';

type AuthorizationProviderProps = {
  children: ReactNode;
};

/**
 * Syncs backend-issued roles/permissions into Redux and exposes Permission + Role providers.
 *
 * Hydration order:
 * 1. Immediate mirror from session profile (login/bootstrap)
 * 2. TanStack Query refresh from {@code GET /auth/authorization}
 */
export function AuthorizationProvider({ children }: AuthorizationProviderProps) {
  const dispatch = useAppDispatch();
  const { status, isAuthenticated, user } = useSession();
  const authorizationQuery = useAuthorizationQuery(isAuthenticated);

  useEffect(() => {
    if (status === 'loading') {
      dispatch(authorizationLoading());
      return;
    }

    if (!isAuthenticated || !user) {
      dispatch(authorizationCleared());
      return;
    }

    dispatch(
      authorizationHydrated({
        userId: user.id,
        tenantId: user.tenantId,
        email: user.email,
        roles: user.roles ?? [],
        permissions: user.permissions ?? [],
      }),
    );
  }, [status, isAuthenticated, user, dispatch]);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }
    if (authorizationQuery.isFetching && !authorizationQuery.data) {
      dispatch(authorizationLoading());
    }
    if (authorizationQuery.data) {
      dispatch(authorizationFromContext(authorizationQuery.data));
    }
    if (authorizationQuery.isError) {
      const message =
        authorizationQuery.error instanceof Error
          ? authorizationQuery.error.message
          : 'Failed to load authorization context';
      // Keep profile-hydrated grants; mark soft error for observability.
      dispatch(authorizationFailed(message));
    }
  }, [
    isAuthenticated,
    authorizationQuery.isFetching,
    authorizationQuery.data,
    authorizationQuery.isError,
    authorizationQuery.error,
    dispatch,
  ]);

  return (
    <RoleProvider>
      <PermissionProvider>{children}</PermissionProvider>
    </RoleProvider>
  );
}
