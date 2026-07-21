import { toast } from 'sonner';

import { authApi } from '@/features/auth/api/auth-api';
import { ResendVerificationForm } from '@/features/auth/components/resend-verification-form';

import { renderWithProviders, screen, userEvent, waitFor } from './test-utils';

jest.mock('sonner', () => ({
  toast: { success: jest.fn(), error: jest.fn() },
}));

jest.mock('@/features/auth/api/auth-api', () => ({
  authApi: { resendVerification: jest.fn() },
}));

const mockedAuthApi = authApi as jest.Mocked<typeof authApi>;

describe('ResendVerificationForm', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders the work email field', () => {
    renderWithProviders(<ResendVerificationForm />);

    expect(screen.getByLabelText('Work email')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /resend verification link/i })).toBeInTheDocument();
  });

  it('shows a validation error when submitting an empty form', async () => {
    const user = userEvent.setup();
    renderWithProviders(<ResendVerificationForm />);

    await user.click(screen.getByRole('button', { name: /resend verification link/i }));

    expect(await screen.findByText('Email is required')).toBeInTheDocument();
    expect(mockedAuthApi.resendVerification).not.toHaveBeenCalled();
  });

  it('shows a generic success message after a valid submission', async () => {
    mockedAuthApi.resendVerification.mockResolvedValueOnce('If unverified, a link was sent.');

    const user = userEvent.setup();
    renderWithProviders(<ResendVerificationForm />);

    await user.type(screen.getByLabelText('Work email'), 'user@hospital.com');
    await user.click(screen.getByRole('button', { name: /resend verification link/i }));

    await waitFor(() =>
      expect(mockedAuthApi.resendVerification).toHaveBeenCalledWith({ email: 'user@hospital.com' }),
    );
    expect(await screen.findByText('Check your email')).toBeInTheDocument();
    expect(toast.success).toHaveBeenCalledWith('Check your email for next steps');
  });

  it('shows an error message when the request fails', async () => {
    mockedAuthApi.resendVerification.mockRejectedValueOnce(new Error('Network error'));

    const user = userEvent.setup();
    renderWithProviders(<ResendVerificationForm />);

    await user.type(screen.getByLabelText('Work email'), 'user@hospital.com');
    await user.click(screen.getByRole('button', { name: /resend verification link/i }));

    expect(await screen.findByText('Request failed')).toBeInTheDocument();
    expect(screen.getByText('Network error')).toBeInTheDocument();
    expect(toast.error).toHaveBeenCalledWith('Network error');
  });
});
