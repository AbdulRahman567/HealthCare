import type { Metadata } from 'next';
import Link from 'next/link';

import { AuthShell } from '@/components/layouts/auth-shell';
import { AuthFormMessage } from '@/features/auth/components/auth-form-message';

export const metadata: Metadata = {
  title: 'Email Verified | Healthcare HMS',
  description: 'Your Healthcare HMS email address has been verified',
};

export default function VerifyEmailSuccessPage() {
  return (
    <AuthShell
      title="Email verified"
      description="Your account email is confirmed."
      footer={
        <p>
          Ready to continue?{' '}
          <Link href="/login" className="text-primary font-medium hover:underline">
            Sign in
          </Link>
        </p>
      }
    >
      <AuthFormMessage
        variant="success"
        title="Verification complete"
        description="Your email address has been verified. You can now sign in to Healthcare HMS."
      />
      <Link
        href="/login"
        className="bg-primary text-primary-foreground hover:bg-primary/80 mt-5 inline-flex h-10 w-full items-center justify-center rounded-lg text-sm font-medium"
      >
        Continue to sign in
      </Link>
    </AuthShell>
  );
}
