import { render, screen, waitFor } from '@testing-library/react';
import { useRouter, useSearchParams } from 'next/navigation';

import { authApi } from '@/features/auth/api/auth-api';
import { VerifyEmailHandler } from '@/features/auth/components/verify-email-handler';
import { ApiClientError } from '@/lib/api-error';

jest.mock('next/navigation', () => ({
  useRouter: jest.fn(),
  useSearchParams: jest.fn(),
}));

jest.mock('@/features/auth/api/auth-api', () => ({
  authApi: { verifyEmail: jest.fn() },
}));

const mockedAuthApi = authApi as jest.Mocked<typeof authApi>;
const mockReplace = jest.fn();
const VALID_TOKEN = 'a'.repeat(32);

describe('VerifyEmailHandler', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (useRouter as jest.Mock).mockReturnValue({ replace: mockReplace });
  });

  it('renders a loading indicator', () => {
    (useSearchParams as jest.Mock).mockReturnValue(new URLSearchParams(`token=${VALID_TOKEN}`));
    mockedAuthApi.verifyEmail.mockReturnValueOnce(new Promise(() => {}));

    render(<VerifyEmailHandler />);

    expect(screen.getByText('Verifying your email address…')).toBeInTheDocument();
  });

  it('redirects to the failed page with reason=missing when no token is present', async () => {
    (useSearchParams as jest.Mock).mockReturnValue(new URLSearchParams());

    render(<VerifyEmailHandler />);

    await waitFor(() =>
      expect(mockReplace).toHaveBeenCalledWith('/verify-email/failed?reason=missing'),
    );
    expect(mockedAuthApi.verifyEmail).not.toHaveBeenCalled();
  });

  it('redirects to the failed page with reason=missing when the token is too short', async () => {
    (useSearchParams as jest.Mock).mockReturnValue(new URLSearchParams('token=short'));

    render(<VerifyEmailHandler />);

    await waitFor(() =>
      expect(mockReplace).toHaveBeenCalledWith('/verify-email/failed?reason=missing'),
    );
    expect(mockedAuthApi.verifyEmail).not.toHaveBeenCalled();
  });

  it('redirects to the success page when verification succeeds', async () => {
    (useSearchParams as jest.Mock).mockReturnValue(new URLSearchParams(`token=${VALID_TOKEN}`));
    mockedAuthApi.verifyEmail.mockResolvedValueOnce('Email verified');

    render(<VerifyEmailHandler />);

    expect(mockedAuthApi.verifyEmail).toHaveBeenCalledWith({ token: VALID_TOKEN });
    await waitFor(() => expect(mockReplace).toHaveBeenCalledWith('/verify-email/success'));
  });

  it('redirects with reason=expired for an expired token error', async () => {
    (useSearchParams as jest.Mock).mockReturnValue(new URLSearchParams(`token=${VALID_TOKEN}`));
    mockedAuthApi.verifyEmail.mockRejectedValueOnce(
      new ApiClientError('Token expired', 400, 'AUTH_EXPIRED_TOKEN'),
    );

    render(<VerifyEmailHandler />);

    await waitFor(() =>
      expect(mockReplace).toHaveBeenCalledWith('/verify-email/failed?reason=expired'),
    );
  });

  it('redirects with reason=invalid for an invalid token error', async () => {
    (useSearchParams as jest.Mock).mockReturnValue(new URLSearchParams(`token=${VALID_TOKEN}`));
    mockedAuthApi.verifyEmail.mockRejectedValueOnce(
      new ApiClientError('Token invalid', 400, 'AUTH_INVALID_TOKEN'),
    );

    render(<VerifyEmailHandler />);

    await waitFor(() =>
      expect(mockReplace).toHaveBeenCalledWith('/verify-email/failed?reason=invalid'),
    );
  });

  it('redirects with reason=failed for any other error', async () => {
    (useSearchParams as jest.Mock).mockReturnValue(new URLSearchParams(`token=${VALID_TOKEN}`));
    mockedAuthApi.verifyEmail.mockRejectedValueOnce(new Error('Unexpected error'));

    render(<VerifyEmailHandler />);

    await waitFor(() =>
      expect(mockReplace).toHaveBeenCalledWith('/verify-email/failed?reason=failed'),
    );
  });

  it('trims whitespace from the token before validating', async () => {
    (useSearchParams as jest.Mock).mockReturnValue(
      new URLSearchParams(`token=${encodeURIComponent(`  ${VALID_TOKEN}  `)}`),
    );
    mockedAuthApi.verifyEmail.mockResolvedValueOnce('Email verified');

    render(<VerifyEmailHandler />);

    await waitFor(() =>
      expect(mockedAuthApi.verifyEmail).toHaveBeenCalledWith({ token: VALID_TOKEN }),
    );
  });
});
