import { toast } from 'sonner';

import { authApi } from '@/features/auth/api/auth-api';
import { ForgotPasswordForm } from '@/features/auth/components/forgot-password-form';

import { renderWithProviders, screen, userEvent, waitFor } from './test-utils';

jest.mock('sonner', () => ({
  toast: { success: jest.fn(), error: jest.fn() },
}));

jest.mock('@/features/auth/api/auth-api', () => ({
  authApi: { forgotPassword: jest.fn() },
}));

const mockedAuthApi = authApi as jest.Mocked<typeof authApi>;

describe('ForgotPasswordForm', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders the work email field', () => {
    renderWithProviders(<ForgotPasswordForm />);

    expect(screen.getByLabelText('Work email')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /send reset link/i })).toBeInTheDocument();
  });

  it('shows a validation error when submitting an empty form', async () => {
    const user = userEvent.setup();
    renderWithProviders(<ForgotPasswordForm />);

    await user.click(screen.getByRole('button', { name: /send reset link/i }));

    expect(await screen.findByText('Email is required')).toBeInTheDocument();
    expect(mockedAuthApi.forgotPassword).not.toHaveBeenCalled();
  });

  it('shows a generic success message after a valid submission', async () => {
    mockedAuthApi.forgotPassword.mockResolvedValueOnce('If an account exists, an email was sent.');

    const user = userEvent.setup();
    renderWithProviders(<ForgotPasswordForm />);

    await user.type(screen.getByLabelText('Work email'), 'user@hospital.com');
    await user.click(screen.getByRole('button', { name: /send reset link/i }));

    await waitFor(() =>
      expect(mockedAuthApi.forgotPassword).toHaveBeenCalledWith({ email: 'user@hospital.com' }),
    );
    expect(await screen.findByText('Check your email')).toBeInTheDocument();
    expect(toast.success).toHaveBeenCalledWith('Check your email for next steps');
    expect(screen.queryByLabelText('Work email')).not.toBeInTheDocument();
  });

  it('shows an error message when the request fails', async () => {
    mockedAuthApi.forgotPassword.mockRejectedValueOnce(new Error('Network error'));

    const user = userEvent.setup();
    renderWithProviders(<ForgotPasswordForm />);

    await user.type(screen.getByLabelText('Work email'), 'user@hospital.com');
    await user.click(screen.getByRole('button', { name: /send reset link/i }));

    expect(await screen.findByText('Request failed')).toBeInTheDocument();
    expect(screen.getByText('Network error')).toBeInTheDocument();
    expect(toast.error).toHaveBeenCalledWith('Network error');
  });
});
