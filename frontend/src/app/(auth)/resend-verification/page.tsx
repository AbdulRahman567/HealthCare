import type { Metadata } from 'next';
import Link from 'next/link';

import { AuthShell } from '@/components/layouts/auth-shell';
import { ResendVerificationForm } from '@/features/auth/components/resend-verification-form';

export const metadata: Metadata = {
  title: 'Resend Verification | Healthcare HMS',
  description: 'Request a new Healthcare HMS email verification link',
};

export default function ResendVerificationPage() {
  return (
    <AuthShell
      title="Resend verification"
      description="Enter your work email and we will send a new verification link if your account is still unverified."
      footer={
        <p>
          Already verified?{' '}
          <Link href="/login" className="text-primary font-medium hover:underline">
            Sign in
          </Link>
        </p>
      }
    >
      <ResendVerificationForm />
    </AuthShell>
  );
}
