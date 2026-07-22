import type { Metadata } from 'next';
import { Suspense } from 'react';

import { AuthShell } from '@/components/layouts/auth-shell';
import { VerifyEmailHandler } from '@/features/auth/components/verify-email-handler';

export const metadata: Metadata = {
  title: 'Verify Email | Healthcare HMS',
  description: 'Verify your Healthcare HMS email address',
};

export default function VerifyEmailPage() {
  return (
    <AuthShell title="Verify email" description="Confirming your email address for Healthcare HMS.">
      <Suspense fallback={<p className="text-muted-foreground text-sm">Preparing verification…</p>}>
        <VerifyEmailHandler />
      </Suspense>
    </AuthShell>
  );
}
