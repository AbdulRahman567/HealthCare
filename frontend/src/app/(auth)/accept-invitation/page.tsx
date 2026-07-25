import type { Metadata } from 'next';
import Link from 'next/link';
import { Suspense } from 'react';

import { AuthShell } from '@/components/layouts/auth-shell';
import { AcceptInvitationForm } from '@/features/hospital-admin/components/invitations/accept-invitation-form';

export const metadata: Metadata = {
  title: 'Accept Invitation | Healthcare HMS',
  description: 'Accept your hospital staff invitation and create your account',
};

export default function AcceptInvitationPage() {
  return (
    <AuthShell
      title="Accept invitation"
      description="Create your Healthcare HMS account using the invitation from your hospital."
      footer={
        <p>
          Already have an account?{' '}
          <Link href="/login" className="text-primary font-medium hover:underline">
            Sign in
          </Link>
        </p>
      }
    >
      <Suspense fallback={<p className="text-muted-foreground text-sm">Loading invitation…</p>}>
        <AcceptInvitationForm />
      </Suspense>
    </AuthShell>
  );
}
