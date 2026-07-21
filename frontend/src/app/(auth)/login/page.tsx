import type { Metadata } from 'next';
import Link from 'next/link';
import { Suspense } from 'react';

import { AuthShell } from '@/components/layouts/auth-shell';
import { LoginForm } from '@/features/auth/components/login-form';

export const metadata: Metadata = {
  title: 'Sign in | Healthcare HMS',
  description: 'Sign in to your Healthcare HMS account',
};

export default function LoginPage() {
  return (
    <AuthShell
      title="Sign in"
      description="Enter your hospital credentials to access Healthcare HMS."
      footer={
        <p>
          New hospital?{' '}
          <Link href="/register/hospital" className="text-primary font-medium hover:underline">
            Register your hospital
          </Link>
        </p>
      }
    >
      <Suspense fallback={<p className="text-muted-foreground text-sm">Loading sign-in form…</p>}>
        <LoginForm />
      </Suspense>
    </AuthShell>
  );
}
