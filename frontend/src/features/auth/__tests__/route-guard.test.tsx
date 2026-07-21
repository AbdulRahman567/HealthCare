import { useRouter } from 'next/navigation';

import { RouteGuard } from '@/features/auth/components/route-guard';
import { useSession } from '@/providers/session-provider';

import { createMockSession, renderWithProviders, screen } from './test-utils';

jest.mock('next/navigation', () => ({
  useRouter: jest.fn(),
}));

jest.mock('@/providers/session-provider', () => ({
  useSession: jest.fn(),
}));

const mockReplace = jest.fn();

describe('RouteGuard', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (useRouter as jest.Mock).mockReturnValue({ replace: mockReplace });
  });

  it('shows a loading state and does not redirect while the session is loading', () => {
    (useSession as jest.Mock).mockReturnValue(createMockSession({ status: 'loading' }));

    renderWithProviders(
      <RouteGuard>
        <p>Protected content</p>
      </RouteGuard>,
    );

    expect(screen.getByText('Checking session…')).toBeInTheDocument();
    expect(mockReplace).not.toHaveBeenCalled();
    expect(screen.queryByText('Protected content')).not.toBeInTheDocument();
  });

  it('redirects to /login when unauthenticated', () => {
    (useSession as jest.Mock).mockReturnValue(
      createMockSession({ status: 'unauthenticated', isAuthenticated: false }),
    );

    renderWithProviders(
      <RouteGuard>
        <p>Protected content</p>
      </RouteGuard>,
    );

    expect(mockReplace).toHaveBeenCalledWith('/login');
    expect(screen.queryByText('Protected content')).not.toBeInTheDocument();
  });

  it('redirects to a custom loginRedirect when provided', () => {
    (useSession as jest.Mock).mockReturnValue(
      createMockSession({ status: 'unauthenticated', isAuthenticated: false }),
    );

    renderWithProviders(
      <RouteGuard loginRedirect="/custom-login">
        <p>Protected content</p>
      </RouteGuard>,
    );

    expect(mockReplace).toHaveBeenCalledWith('/custom-login');
  });

  it('renders children when authenticated and no roles/permissions are required', () => {
    (useSession as jest.Mock).mockReturnValue(
      createMockSession({ status: 'authenticated', isAuthenticated: true }),
    );

    renderWithProviders(
      <RouteGuard>
        <p>Protected content</p>
      </RouteGuard>,
    );

    expect(mockReplace).not.toHaveBeenCalled();
    expect(screen.getByText('Protected content')).toBeInTheDocument();
  });

  it('redirects to /forbidden when authenticated but missing the required role', () => {
    const hasRole = jest.fn().mockReturnValue(false);
    (useSession as jest.Mock).mockReturnValue(
      createMockSession({ status: 'authenticated', isAuthenticated: true, hasRole }),
    );

    renderWithProviders(
      <RouteGuard roles={['ADMIN']}>
        <p>Protected content</p>
      </RouteGuard>,
    );

    expect(hasRole).toHaveBeenCalledWith(['ADMIN']);
    expect(mockReplace).toHaveBeenCalledWith('/forbidden');
    expect(screen.queryByText('Protected content')).not.toBeInTheDocument();
  });

  it('redirects to a custom forbiddenRedirect when missing the required permission', () => {
    const hasPermission = jest.fn().mockReturnValue(false);
    (useSession as jest.Mock).mockReturnValue(
      createMockSession({ status: 'authenticated', isAuthenticated: true, hasPermission }),
    );

    renderWithProviders(
      <RouteGuard permissions={['users:write']} forbiddenRedirect="/no-access">
        <p>Protected content</p>
      </RouteGuard>,
    );

    expect(hasPermission).toHaveBeenCalledWith(['users:write']);
    expect(mockReplace).toHaveBeenCalledWith('/no-access');
  });

  it('renders children when the user holds the required role and permission', () => {
    const hasRole = jest.fn().mockReturnValue(true);
    const hasPermission = jest.fn().mockReturnValue(true);
    (useSession as jest.Mock).mockReturnValue(
      createMockSession({
        status: 'authenticated',
        isAuthenticated: true,
        hasRole,
        hasPermission,
      }),
    );

    renderWithProviders(
      <RouteGuard roles={['ADMIN']} permissions={['users:write']}>
        <p>Protected content</p>
      </RouteGuard>,
    );

    expect(mockReplace).not.toHaveBeenCalled();
    expect(screen.getByText('Protected content')).toBeInTheDocument();
  });
});
