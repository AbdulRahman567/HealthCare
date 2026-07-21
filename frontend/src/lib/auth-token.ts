import {
  ACCESS_TOKEN_STORAGE_KEY,
  REFRESH_TOKEN_STORAGE_KEY,
  SESSION_FLAG_COOKIE,
} from '@/lib/session-constants';

/**
 * Access + refresh token store backed by sessionStorage for tab-scoped persistence.
 * Note: SECURITY.md prefers HttpOnly cookies; migrating to cookie-based sessions is tracked
 * as a remaining hardening item. Until then, keep XSS surface minimal and sync the edge cookie.
 */
let memoryAccessToken: string | null = null;
let memoryRefreshToken: string | null = null;

function canUseDom(): boolean {
  return typeof window !== 'undefined';
}

function isSecureContext(): boolean {
  if (!canUseDom()) {
    return false;
  }
  return window.location.protocol === 'https:';
}

function syncSessionCookie(hasSession: boolean): void {
  if (!canUseDom()) {
    return;
  }
  const secure = isSecureContext() ? '; Secure' : '';
  if (hasSession) {
    document.cookie = `${SESSION_FLAG_COOKIE}=1; path=/; SameSite=Lax${secure}`;
  } else {
    document.cookie = `${SESSION_FLAG_COOKIE}=; path=/; Max-Age=0; SameSite=Lax${secure}`;
  }
}

export const authTokenStore = {
  getAccessToken(): string | null {
    if (memoryAccessToken) {
      return memoryAccessToken;
    }
    if (!canUseDom()) {
      return null;
    }
    memoryAccessToken = window.sessionStorage.getItem(ACCESS_TOKEN_STORAGE_KEY);
    return memoryAccessToken;
  },

  getRefreshToken(): string | null {
    if (memoryRefreshToken) {
      return memoryRefreshToken;
    }
    if (!canUseDom()) {
      return null;
    }
    memoryRefreshToken = window.sessionStorage.getItem(REFRESH_TOKEN_STORAGE_KEY);
    return memoryRefreshToken;
  },

  /**
   * Re-assert the middleware session cookie from existing tokens (e.g. after bootstrap).
   */
  syncSessionFlag(): void {
    syncSessionCookie(Boolean(this.getAccessToken() || this.getRefreshToken()));
  },

  setTokens(accessToken: string | null, refreshToken: string | null): void {
    memoryAccessToken = accessToken;
    memoryRefreshToken = refreshToken;

    if (!canUseDom()) {
      return;
    }

    if (accessToken) {
      window.sessionStorage.setItem(ACCESS_TOKEN_STORAGE_KEY, accessToken);
    } else {
      window.sessionStorage.removeItem(ACCESS_TOKEN_STORAGE_KEY);
    }

    if (refreshToken) {
      window.sessionStorage.setItem(REFRESH_TOKEN_STORAGE_KEY, refreshToken);
    } else {
      window.sessionStorage.removeItem(REFRESH_TOKEN_STORAGE_KEY);
    }

    syncSessionCookie(Boolean(accessToken || refreshToken));
  },

  set(token: string | null): void {
    this.setTokens(token, this.getRefreshToken());
  },

  clear(): void {
    this.setTokens(null, null);
  },
};
