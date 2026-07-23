import { configureStore } from '@reduxjs/toolkit';
import { render, screen } from '@testing-library/react';
import type { ReactElement } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import { Provider } from 'react-redux';

import { createMockSession } from '@/features/auth/__tests__/test-utils';
import { RouteProtection } from '@/features/authorization/components/route-protection';
import { PermissionProvider } from '@/features/authorization/providers/permission-provider';
import { RoleProvider } from '@/features/authorization/providers/role-provider';
import { authorizationReducer } from '@/features/authorization/store/authorization-slice';
import { useSession } from '@/providers/session-provider';

jest.mock('next/navigation', () => ({
  useRouter: jest.fn(),
  usePathname: jest.fn(),
}));

jest.mock('@/providers/session-provider', () => ({
  useSession: jest.fn(),
}));

const mockReplace = jest.fn();

function renderProtected(
  ui: ReactElement,
  options: {
    session?: ReturnType<typeof createMockSession>;
    roles?: string[];
    permissions?: string[];
    pathname?: string;
  } = {},
) {
  (useRouter as jest.Mock).mockReturnValue({ replace: mockReplace });
  (usePathname as jest.Mock).mockReturnValue(options.pathname ?? '/app');
  (useSession as jest.Mock).mockReturnValue(
    options.session ??
      createMockSession({ status: 'authenticated', isAuthenticated: true }),
  );

  const store = configureStore({
    reducer: { authorization: authorizationReducer },
    preloadedState: {
      authorization: {
        status: 'ready' as const,
        userId: 'u1',
        tenantId: 't1',
        email: 'a@b.com',
        roles: options.roles ?? ['HOSPITAL_ADMIN'],
        permissions: options.permissions ?? ['HOSPITAL_READ', 'USER_READ'],
        error: null,
      },
    },
  });

  return render(
    <Provider store={store}>
      <RoleProvider>
        <PermissionProvider>
          <RouteProtection useRouteCatalog>{ui}</RouteProtection>
        </PermissionProvider>
      </RoleProvider>
    </Provider>,
  );
}

describe('RouteProtection', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('redirects to login when unauthenticated', () => {
    renderProtected(<p>Secret</p>, {
      session: createMockSession({ status: 'unauthenticated', isAuthenticated: false }),
      pathname: '/app/profile',
    });

    expect(mockReplace).toHaveBeenCalledWith('/login?next=%2Fapp%2Fprofile');
    expect(screen.queryByText('Secret')).not.toBeInTheDocument();
  });

  it('redirects to forbidden when missing route permission', () => {
    renderProtected(<p>Users</p>, {
      pathname: '/app/users',
      permissions: ['HOSPITAL_READ'],
      roles: ['DOCTOR'],
    });

    expect(mockReplace).toHaveBeenCalledWith('/forbidden');
    expect(screen.queryByText('Users')).not.toBeInTheDocument();
  });

  it('renders the page when the principal satisfies the route catalog', () => {
    renderProtected(<p>Users</p>, {
      pathname: '/app/users',
      permissions: ['USER_READ'],
      roles: ['HOSPITAL_ADMIN'],
    });

    expect(mockReplace).not.toHaveBeenCalled();
    expect(screen.getByText('Users')).toBeInTheDocument();
  });

  it('allows dashboard when DASHBOARD_READ is granted', () => {
    renderProtected(<p>Home</p>, {
      pathname: '/app',
      permissions: ['DASHBOARD_READ'],
    });

    expect(mockReplace).not.toHaveBeenCalled();
    expect(screen.getByText('Home')).toBeInTheDocument();
  });

  it('denies dashboard without DASHBOARD_READ', () => {
    renderProtected(<p>Home</p>, {
      pathname: '/app',
      permissions: ['USER_READ'],
    });

    expect(mockReplace).toHaveBeenCalledWith('/forbidden');
    expect(screen.queryByText('Home')).not.toBeInTheDocument();
  });

  it('denies unknown /app routes (fail-closed catalog)', () => {
    renderProtected(<p>Secret module</p>, {
      pathname: '/app/unknown-module',
      permissions: ['USER_READ', 'HOSPITAL_READ'],
    });

    expect(mockReplace).toHaveBeenCalledWith('/forbidden');
    expect(screen.queryByText('Secret module')).not.toBeInTheDocument();
  });
});