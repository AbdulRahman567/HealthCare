import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { fireEvent, render, screen, waitFor, type RenderOptions } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import type { ReactElement, ReactNode } from 'react';

import type { UserProfile } from '@/features/auth/types/auth.types';

export { fireEvent, screen, waitFor, userEvent };

/** Sets an input value in one change event (avoids slow/flaky per-key typing). */
export function fillInput(element: HTMLElement, value: string) {
  fireEvent.change(element, { target: { value } });
}

function createTestQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });
}

export function renderWithProviders(ui: ReactElement, options?: Omit<RenderOptions, 'wrapper'>) {
  const queryClient = createTestQueryClient();

  function Wrapper({ children }: { children: ReactNode }) {
    return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
  }

  return {
    queryClient,
    ...render(ui, { wrapper: Wrapper, ...options }),
  };
}

type SessionOverrides = {
  status?: 'loading' | 'authenticated' | 'unauthenticated';
  user?: UserProfile | null;
  isAuthenticated?: boolean;
  accessToken?: string | null;
  signIn?: jest.Mock;
  signOut?: jest.Mock;
  refreshProfile?: jest.Mock;
  hasRole?: (role: string | string[]) => boolean;
  hasPermission?: (permission: string | string[]) => boolean;
};

export function createMockSession(overrides: SessionOverrides = {}) {
  const status = overrides.status ?? 'unauthenticated';
  const isAuthenticated = overrides.isAuthenticated ?? status === 'authenticated';

  return {
    status,
    user: overrides.user ?? null,
    accessToken: overrides.accessToken ?? null,
    isAuthenticated,
    hasRole: overrides.hasRole ?? (() => true),
    hasPermission: overrides.hasPermission ?? (() => true),
    signIn: overrides.signIn ?? jest.fn(),
    signOut: overrides.signOut ?? jest.fn(),
    refreshProfile: overrides.refreshProfile ?? jest.fn(),
  };
}
