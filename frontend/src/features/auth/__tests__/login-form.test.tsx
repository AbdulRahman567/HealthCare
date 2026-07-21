import { useRouter, useSearchParams } from 'next/navigation';
import { toast } from 'sonner';

import { authApi } from '@/features/auth/api/auth-api';
import { LoginForm } from '@/features/auth/components/login-form';
import { ApiClientError } from '@/lib/api-error';
import { useSession } from '@/providers/session-provider';

import {
  createMockSession,
  fillInput,
  renderWithProviders,
  screen,
  userEvent,
  waitFor,
} from './test-utils';

jest.mock('next/navigation', () => ({
  useRouter: jest.fn(),
  useSearchParams: jest.fn(),
}));

jest.mock('sonner', () => ({
  toast: { success: jest.fn(), error: jest.fn() },
}));

jest.mock('@/features/auth/api/auth-api', () => ({
  authApi: { login: jest.fn() },
}));

jest.mock('@/providers/session-provider', () => ({
  useSession: jest.fn(),
}));

const mockedAuthApi = authApi as jest.Mocked<typeof authApi>;
const mockReplace = jest.fn();

describe('LoginForm', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (useRouter as jest.Mock).mockReturnValue({ replace: mockReplace });
    (useSearchParams as jest.Mock).mockReturnValue(new URLSearchParams());
    (useSession as jest.Mock).mockReturnValue(createMockSession());
  });

  it('renders the email and password fields', () => {
    renderWithProviders(<LoginForm />);

    expect(screen.getByLabelText('Email')).toBeInTheDocument();
    expect(screen.getByLabelText('Password')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /forgot password/i })).toHaveAttribute(
      'href',
      '/forgot-password',
    );
  });

  it('shows validation errors when submitting an empty form', async () => {
    const user = userEvent.setup();
    renderWithProviders(<LoginForm />);

    await user.click(screen.getByRole('button', { name: /sign in/i }));

    expect(await screen.findByText('Email is required')).toBeInTheDocument();
    expect(screen.getByText('Password is required')).toBeInTheDocument();
    expect(mockedAuthApi.login).not.toHaveBeenCalled();
  });

  it('shows an error for a malformed email', async () => {
    const user = userEvent.setup();
    renderWithProviders(<LoginForm />);

    fillInput(screen.getByLabelText('Email'), 'not-an-email');
    fillInput(screen.getByLabelText('Password'), 'password123');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    expect(await screen.findByText('Enter a valid email address')).toBeInTheDocument();
  });

  it('signs in, shows a success toast, and redirects to /app on success', async () => {
    const signIn = jest.fn();
    (useSession as jest.Mock).mockReturnValue(createMockSession({ signIn }));

    const authResult = {
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      tokenType: 'Bearer',
      expiresInSeconds: 900,
      refreshExpiresInSeconds: 3600,
      user: {
        id: 'user-1',
        tenantId: 'tenant-1',
        firstName: 'Ada',
        lastName: 'Lovelace',
        email: 'ada@hospital.com',
        phone: null,
        emailVerified: true,
        emailVerifiedAt: null,
        status: 'ACTIVE' as const,
        roles: ['ADMIN'],
        permissions: [],
        lastLoginAt: null,
        createdAt: '2026-01-01T00:00:00.000Z',
      },
    };
    mockedAuthApi.login.mockResolvedValueOnce(authResult);

    const user = userEvent.setup();
    renderWithProviders(<LoginForm />);

    fillInput(screen.getByLabelText('Email'), 'ada@hospital.com');
    fillInput(screen.getByLabelText('Password'), 'CorrectPass1!');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => expect(mockedAuthApi.login).toHaveBeenCalledWith({
      email: 'ada@hospital.com',
      password: 'CorrectPass1!',
    }));
    await waitFor(() => expect(signIn).toHaveBeenCalledWith(authResult));
    expect(toast.success).toHaveBeenCalledWith('Signed in successfully');
    expect(mockReplace).toHaveBeenCalledWith('/app');
  });

  it('redirects to the "next" query param when present and safe', async () => {
    (useSearchParams as jest.Mock).mockReturnValue(new URLSearchParams('next=/app/profile'));
    mockedAuthApi.login.mockResolvedValueOnce({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      tokenType: 'Bearer',
      expiresInSeconds: 900,
      refreshExpiresInSeconds: 3600,
      user: {
        id: 'user-1',
        tenantId: null,
        firstName: 'Ada',
        lastName: 'Lovelace',
        email: 'ada@hospital.com',
        phone: null,
        emailVerified: true,
        emailVerifiedAt: null,
        status: 'ACTIVE' as const,
        roles: [],
        permissions: [],
        lastLoginAt: null,
        createdAt: '2026-01-01T00:00:00.000Z',
      },
    });

    const user = userEvent.setup();
    renderWithProviders(<LoginForm />);

    fillInput(screen.getByLabelText('Email'), 'ada@hospital.com');
    fillInput(screen.getByLabelText('Password'), 'CorrectPass1!');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => expect(mockReplace).toHaveBeenCalledWith('/app/profile'));
  });

  it('ignores an unsafe "next" query param and falls back to /app', async () => {
    (useSearchParams as jest.Mock).mockReturnValue(new URLSearchParams('next=https://evil.example.com'));
    mockedAuthApi.login.mockResolvedValueOnce({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      tokenType: 'Bearer',
      expiresInSeconds: 900,
      refreshExpiresInSeconds: 3600,
      user: {
        id: 'user-1',
        tenantId: null,
        firstName: 'Ada',
        lastName: 'Lovelace',
        email: 'ada@hospital.com',
        phone: null,
        emailVerified: true,
        emailVerifiedAt: null,
        status: 'ACTIVE' as const,
        roles: [],
        permissions: [],
        lastLoginAt: null,
        createdAt: '2026-01-01T00:00:00.000Z',
      },
    });

    const user = userEvent.setup();
    renderWithProviders(<LoginForm />);

    fillInput(screen.getByLabelText('Email'), 'ada@hospital.com');
    fillInput(screen.getByLabelText('Password'), 'CorrectPass1!');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => expect(mockReplace).toHaveBeenCalledWith('/app'));
  });

  it('shows an error message and a resend-verification link when the email is unverified', async () => {
    mockedAuthApi.login.mockRejectedValueOnce(
      new ApiClientError('Please verify your email first', 403, 'EMAIL_NOT_VERIFIED'),
    );

    const user = userEvent.setup();
    renderWithProviders(<LoginForm />);

    fillInput(screen.getByLabelText('Email'), 'ada@hospital.com');
    fillInput(screen.getByLabelText('Password'), 'CorrectPass1!');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    expect(await screen.findByText('Sign in failed')).toBeInTheDocument();
    expect(screen.getByText('Please verify your email first')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /resend verification email/i })).toHaveAttribute(
      'href',
      '/resend-verification',
    );
    expect(toast.error).toHaveBeenCalledWith('Please verify your email first');
  });

  it('shows a generic error message and no resend link for other failures', async () => {
    mockedAuthApi.login.mockRejectedValueOnce(new ApiClientError('Invalid credentials', 401));

    const user = userEvent.setup();
    renderWithProviders(<LoginForm />);

    fillInput(screen.getByLabelText('Email'), 'ada@hospital.com');
    fillInput(screen.getByLabelText('Password'), 'WrongPass1!');
    await user.click(screen.getByRole('button', { name: /sign in/i }));

    expect(await screen.findByText('Invalid credentials')).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /resend verification email/i })).not.toBeInTheDocument();
  });
});
