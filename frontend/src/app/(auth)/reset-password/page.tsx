import type { Metadata } from 'next';
import Link from 'next/link';
import { Suspense } from 'react';

import { AuthShell } from '@/components/layouts/auth-shell';
import { ResetPasswordForm } from '@/features/auth/components/reset-password-form';

export const metadata: Metadata = {
  title: 'Reset Password | Healthcare HMS',
  description: 'Choose a new password for your Healthcare HMS account',
};

export default function ResetPasswordPage() {
  return (
    <AuthShell
      title="Reset password"
      description="Choose a strong new password for your Healthcare HMS account."
      footer={
        <p>
          Remembered your password?{' '}
          <Link href="/login" className="text-primary font-medium hover:underline">
            Back to sign in
          </Link>
        </p>
      }
    >
      <Suspense fallback={<p className="text-muted-foreground text-sm">Loading reset form…</p>}>
        <ResetPasswordForm />
      </Suspense>
    </AuthShell>
  );
}
