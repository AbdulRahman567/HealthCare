import type { Metadata } from 'next';
import Link from 'next/link';

import { AuthShell } from '@/components/layouts/auth-shell';
import { AuthFormMessage } from '@/features/auth/components/auth-form-message';

export const metadata: Metadata = {
  title: 'Registration Complete | Healthcare HMS',
  description: 'Your Healthcare HMS hospital account has been created',
};

type Props = {
  searchParams: Promise<{ tenant?: string; email?: string }>;
};

export default async function VerifyRegistrationSuccessPage({ searchParams }: Props) {
  const { tenant, email } = await searchParams;
  return (
    <AuthShell
      title="Registration complete"
      description="Your hospital account has been created."
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
        title="Your hospital account is ready"
        description={`Tenant "${tenant ?? 'your hospital'}" has been created and administrator ${email ?? 'you'} can now sign in. Your free trial starts now.`}
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
