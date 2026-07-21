import { useRouter, useSearchParams } from 'next/navigation';
import { toast } from 'sonner';

import { authApi } from '@/features/auth/api/auth-api';
import { ResetPasswordForm } from '@/features/auth/components/reset-password-form';

import { fillInput, renderWithProviders, screen, userEvent, waitFor } from './test-utils';

jest.mock('next/navigation', () => ({
  useRouter: jest.fn(),
  useSearchParams: jest.fn(),
}));

jest.mock('sonner', () => ({
  toast: { success: jest.fn(), error: jest.fn() },
}));

jest.mock('@/features/auth/api/auth-api', () => ({
  authApi: { resetPassword: jest.fn() },
}));

const mockedAuthApi = authApi as jest.Mocked<typeof authApi>;
const mockReplace = jest.fn();
const VALID_TOKEN = 'a'.repeat(32);
const STRONG_PASSWORD = 'Sup3r-Secret!!';

describe('ResetPasswordForm', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (useRouter as jest.Mock).mockReturnValue({ replace: mockReplace });
  });

  it('shows an invalid-link error and no form when the token is missing', () => {
    (useSearchParams as jest.Mock).mockReturnValue(new URLSearchParams());

    renderWithProviders(<ResetPasswordForm />);

    expect(screen.getByText('Invalid reset link')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /request a new link/i })).toHaveAttribute(
      'href',
      '/forgot-password',
    );
    expect(screen.queryByLabelText('New password')).not.toBeInTheDocument();
  });

  it('shows an invalid-link error when the token is too short', () => {
    (useSearchParams as jest.Mock).mockReturnValue(new URLSearchParams('token=short'));

    renderWithProviders(<ResetPasswordForm />);

    expect(screen.getByText('Invalid reset link')).toBeInTheDocument();
  });

  it('renders the password fields when a valid token is present', () => {
    (useSearchParams as jest.Mock).mockReturnValue(new URLSearchParams(`token=${VALID_TOKEN}`));

    renderWithProviders(<ResetPasswordForm />);

    expect(screen.getByLabelText('New password')).toBeInTheDocument();
    expect(screen.getByLabelText('Confirm password')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /reset password/i })).toBeInTheDocument();
  });

  it('shows a validation error for a mismatched confirmation', async () => {
    (useSearchParams as jest.Mock).mockReturnValue(new URLSearchParams(`token=${VALID_TOKEN}`));

    const user = userEvent.setup();
    renderWithProviders(<ResetPasswordForm />);

    fillInput(screen.getByLabelText('New password'), STRONG_PASSWORD);
    fillInput(screen.getByLabelText('Confirm password'), 'Different123!!');
    await user.click(screen.getByRole('button', { name: /reset password/i }));

    expect(await screen.findByText('Passwords do not match')).toBeInTheDocument();
    expect(mockedAuthApi.resetPassword).not.toHaveBeenCalled();
  });

  it('submits the token and new password, then redirects to /login', async () => {
    (useSearchParams as jest.Mock).mockReturnValue(new URLSearchParams(`token=${VALID_TOKEN}`));
    mockedAuthApi.resetPassword.mockResolvedValueOnce('Password updated');

    const user = userEvent.setup();
    renderWithProviders(<ResetPasswordForm />);

    fillInput(screen.getByLabelText('New password'), STRONG_PASSWORD);
    fillInput(screen.getByLabelText('Confirm password'), STRONG_PASSWORD);
    await user.click(screen.getByRole('button', { name: /reset password/i }));

    await waitFor(() =>
      expect(mockedAuthApi.resetPassword).toHaveBeenCalledWith({
        token: VALID_TOKEN,
        newPassword: STRONG_PASSWORD,
      }),
    );
    expect(toast.success).toHaveBeenCalledWith('Password updated. You can sign in now.');
    expect(mockReplace).toHaveBeenCalledWith('/login');
  });

  it('shows an error message when the reset request fails', async () => {
    (useSearchParams as jest.Mock).mockReturnValue(new URLSearchParams(`token=${VALID_TOKEN}`));
    mockedAuthApi.resetPassword.mockRejectedValueOnce(new Error('Token expired'));

    const user = userEvent.setup();
    renderWithProviders(<ResetPasswordForm />);

    fillInput(screen.getByLabelText('New password'), STRONG_PASSWORD);
    fillInput(screen.getByLabelText('Confirm password'), STRONG_PASSWORD);
    await user.click(screen.getByRole('button', { name: /reset password/i }));

    expect(await screen.findByText('Reset failed')).toBeInTheDocument();
    expect(screen.getByText('Token expired')).toBeInTheDocument();
    expect(mockReplace).not.toHaveBeenCalledWith('/login');
  });
});
