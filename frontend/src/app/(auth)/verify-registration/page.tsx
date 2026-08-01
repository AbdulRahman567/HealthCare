import type { Metadata } from 'next';
import { Suspense } from 'react';

import { AuthShell } from '@/components/layouts/auth-shell';
import { VerifyRegistrationHandler } from '@/features/auth/components/verify-registration-handler';

export const metadata: Metadata = {
  title: 'Verify Registration | Healthcare HMS',
  description: 'Complete your Healthcare HMS hospital registration',
};

export default function VerifyRegistrationPage() {
  return (
    <AuthShell
      title="Create your hospital account"
      description="Confirming your registration and creating your hospital account."
    >
      <Suspense fallback={<p className="text-muted-foreground text-sm">Preparing registration…</p>}>
        <VerifyRegistrationHandler />
      </Suspense>
    </AuthShell>
  );
}
