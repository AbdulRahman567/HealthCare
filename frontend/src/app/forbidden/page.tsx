import type { Metadata } from 'next';
import Link from 'next/link';

import { buttonVariants } from '@/components/ui/button';
import { AuthFormMessage } from '@/features/auth/components/auth-form-message';
import { cn } from '@/lib/utils';

export const metadata: Metadata = {
  title: 'Forbidden | Healthcare HMS',
  description: 'You do not have permission to view this page',
};

export default function ForbiddenPage() {
  return (
    <main className="mx-auto flex min-h-screen w-full max-w-lg flex-col justify-center gap-6 px-6 py-16">
      <AuthFormMessage
        variant="error"
        title="Access forbidden"
        description="Your account is signed in but does not have permission for this area."
      />
      <div className="flex gap-3">
        <Link href="/app" className={cn(buttonVariants(), 'h-10 px-4')}>
          Back to workspace
        </Link>
        <Link href="/login" className={cn(buttonVariants({ variant: 'outline' }), 'h-10 px-4')}>
          Sign in
        </Link>
      </div>
    </main>
  );
}
