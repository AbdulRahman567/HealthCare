import type { Metadata } from 'next';
import Link from 'next/link';

import { AuthShell } from '@/components/layouts/auth-shell';
import { AuthFormMessage } from '@/features/auth/components/auth-form-message';
import { VerifyEmailFailedDetails } from '@/features/auth/components/verify-email-failed-details';

export const metadata: Metadata = {
  title: 'Registration Verification Failed | Healthcare HMS',
  description: 'Registration verification could not be completed',
};

export default function VerifyRegistrationFailedPage() {
  return (
    <AuthShell
      title="Verification failed"
      description="We could not complete your registration with this link."
      footer={
        <p>
          Want to try again?{' '}
          <Link href="/register/hospital" className="text-primary font-medium hover:underline">
            Start a new registration
          </Link>
        </p>
      }
    >
      <AuthFormMessage
        variant="error"
        title="Unable to create your account"
        description="The verification link may be invalid, expired, or already used."
      />
      <VerifyEmailFailedDetails />
      <Link
        href="/register/hospital"
        className="bg-primary text-primary-foreground hover:bg-primary/80 mt-5 inline-flex h-10 w-full items-center justify-center rounded-lg text-sm font-medium"
      >
        Start a new registration
      </Link>
    </AuthShell>
  );
}
