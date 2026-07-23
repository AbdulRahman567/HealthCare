'use client';

import { useQuery } from '@tanstack/react-query';

import { authApi } from '@/features/auth/api/auth-api';
import type { AuthorizationContext } from '@/features/authorization/types';
import { useSession } from '@/providers/session-provider';

export const authorizationQueryKey = ['authorization', 'context'] as const;

/**
 * Fetches the backend authorization snapshot for the signed-in principal.
 * UX-only — does not replace API enforcement.
 */
export function useAuthorizationQuery(enabled = true) {
  const { isAuthenticated, user } = useSession();

  return useQuery({
    queryKey: [...authorizationQueryKey, user?.id ?? 'anonymous'],
    queryFn: async (): Promise<AuthorizationContext> => {
      const context = await authApi.getAuthorization();
      return {
        userId: context.userId,
        tenantId: context.tenantId,
        email: context.email,
        roles: context.roles ?? [],
        permissions: context.permissions ?? [],
      };
    },
    enabled: enabled && isAuthenticated && Boolean(user?.id),
    staleTime: 60_000,
  });
}
