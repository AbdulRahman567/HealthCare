import { render, screen } from '@testing-library/react';

import { AuthFormMessage } from '@/features/auth/components/auth-form-message';

describe('AuthFormMessage', () => {
  it('renders an error message with title and description', () => {
    render(
      <AuthFormMessage variant="error" title="Sign in failed" description="Invalid credentials" />,
    );

    expect(screen.getByRole('alert')).toBeInTheDocument();
    expect(screen.getByText('Sign in failed')).toBeInTheDocument();
    expect(screen.getByText('Invalid credentials')).toBeInTheDocument();
  });

  it('renders a success message with title and description', () => {
    render(
      <AuthFormMessage variant="success" title="Check your email" description="Link sent" />,
    );

    expect(screen.getByRole('alert')).toBeInTheDocument();
    expect(screen.getByText('Check your email')).toBeInTheDocument();
    expect(screen.getByText('Link sent')).toBeInTheDocument();
  });

  it('renders without a description', () => {
    render(<AuthFormMessage variant="error" title="Something went wrong" />);

    expect(screen.getByText('Something went wrong')).toBeInTheDocument();
  });
});
