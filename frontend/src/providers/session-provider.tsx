'use client';

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';

import { authApi } from '@/features/auth/api/auth-api';
import type { AuthResponse, UserProfile } from '@/features/auth/types/auth.types';
import { onAuthSessionInvalidated } from '@/lib/auth-events';
import { ApiClientError } from '@/lib/api-error';
import { authTokenStore } from '@/lib/auth-token';

type SessionStatus = 'loading' | 'authenticated' | 'unauthenticated';

type SessionContextValue = {
  status: SessionStatus;
  user: UserProfile | null;
  accessToken: string | null;
  isAuthenticated: boolean;
  hasRole: (role: string | string[]) => boolean;
  hasPermission: (permission: string | string[]) => boolean;
  signIn: (auth: AuthResponse) => void;
  signOut: () => Promise<void>;
  refreshProfile: () => Promise<UserProfile | null>;
};

const SessionContext = createContext<SessionContextValue | null>(null);

type SessionProviderProps = {
  children: ReactNode;
};

export function SessionProvider({ children }: SessionProviderProps) {
  const [status, setStatus] = useState<SessionStatus>('loading');
  const [user, setUser] = useState<UserProfile | null>(null);
  const [accessToken, setAccessToken] = useState<string | null>(null);

  const clearSession = useCallback(() => {
    authTokenStore.clear();
    setAccessToken(null);
    setUser(null);
    setStatus('unauthenticated');
  }, []);

  useEffect(() => onAuthSessionInvalidated(clearSession), [clearSession]);

  const refreshProfile = useCallback(async () => {
    const token = authTokenStore.getAccessToken();
    if (!token) {
      clearSession();
      return null;
    }

    try {
      const profile = await authApi.getProfile();
      authTokenStore.syncSessionFlag();
      setAccessToken(authTokenStore.getAccessToken());
      setUser(profile);
      setStatus('authenticated');
      return profile;
    } catch (error) {
      if (error instanceof ApiClientError && (error.status === 401 || error.status === 403)) {
        clearSession();
        return null;
      }
      // Keep prior authenticated state on transient failures; only force logout when never established.
      setStatus((current) => (current === 'authenticated' ? current : 'unauthenticated'));
      throw error;
    }
  }, [clearSession]);

  useEffect(() => {
    void (async () => {
      try {
        let access = authTokenStore.getAccessToken();
        const refresh = authTokenStore.getRefreshToken();

        // Silent authentication: recover session from refresh token when needed.
        if (!access && refresh) {
          const refreshed = await authApi.refresh();
          access = refreshed.accessToken;
          authTokenStore.syncSessionFlag();
          setUser(refreshed.user);
          setAccessToken(access);
          setStatus('authenticated');
          return;
        }

        if (!access && !refresh) {
          setStatus('unauthenticated');
          return;
        }

        // Tokens already present — re-sync middleware cookie in case it was cleared.
        authTokenStore.syncSessionFlag();
        setAccessToken(access);
        await refreshProfile();
      } catch {
        clearSession();
      }
    })();
  }, [clearSession, refreshProfile]);

  const signIn = useCallback((auth: AuthResponse) => {
    authTokenStore.setTokens(auth.accessToken, auth.refreshToken);
    setAccessToken(auth.accessToken);
    setUser(auth.user);
    setStatus('authenticated');
  }, []);

  const signOut = useCallback(async () => {
    try {
      if (authTokenStore.getAccessToken() || authTokenStore.getRefreshToken()) {
        await authApi.logout();
      }
    } finally {
      clearSession();
    }
  }, [clearSession]);

  const hasRole = useCallback(
    (role: string | string[]) => {
      if (!user) {
        return false;
      }
      const required = Array.isArray(role) ? role : [role];
      return required.some((value) => user.roles.includes(value));
    },
    [user],
  );

  const hasPermission = useCallback(
    (permission: string | string[]) => {
      if (!user) {
        return false;
      }
      const required = Array.isArray(permission) ? permission : [permission];
      return required.some((value) => user.permissions.includes(value));
    },
    [user],
  );

  const value = useMemo<SessionContextValue>(
    () => ({
      status,
      user,
      accessToken,
      isAuthenticated: status === 'authenticated' && Boolean(user),
      hasRole,
      hasPermission,
      signIn,
      signOut,
      refreshProfile,
    }),
    [status, user, accessToken, hasRole, hasPermission, signIn, signOut, refreshProfile],
  );

  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>;
}

export function useSession(): SessionContextValue {
  const context = useContext(SessionContext);
  if (!context) {
    throw new Error('useSession must be used within SessionProvider');
  }
  return context;
}
