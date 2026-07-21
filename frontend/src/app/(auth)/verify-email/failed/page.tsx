import type { Metadata } from 'next';
import Link from 'next/link';

import { AuthShell } from '@/components/layouts/auth-shell';
import { AuthFormMessage } from '@/features/auth/components/auth-form-message';
import { VerifyEmailFailedDetails } from '@/features/auth/components/verify-email-failed-details';

export const metadata: Metadata = {
  title: 'Verification Failed | Healthcare HMS',
  description: 'Email verification could not be completed',
};

export default function VerifyEmailFailedPage() {
  return (
    <AuthShell
      title="Verification failed"
      description="We could not verify your email with this link."
      footer={
        <p>
          Need another link?{' '}
          <Link href="/resend-verification" className="text-primary font-medium hover:underline">
            Resend verification
          </Link>
        </p>
      }
    >
      <AuthFormMessage
        variant="error"
        title="Unable to verify email"
        description="The verification link may be invalid, expired, or already used."
      />
      <VerifyEmailFailedDetails />
      <Link
        href="/resend-verification"
        className="bg-primary text-primary-foreground hover:bg-primary/80 mt-5 inline-flex h-10 w-full items-center justify-center rounded-lg text-sm font-medium"
      >
        Request a new verification link
      </Link>
    </AuthShell>
  );
}
