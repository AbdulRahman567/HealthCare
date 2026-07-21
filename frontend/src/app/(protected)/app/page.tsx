import type { Metadata } from 'next';

import { AuthFormMessage } from '@/features/auth/components/auth-form-message';
import { SessionHome } from '@/features/auth/components/session-home';

export const metadata: Metadata = {
  title: 'Workspace | Healthcare HMS',
  description: 'Authenticated workspace home',
};

export default function AppHomePage() {
  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div className="space-y-2">
        <h1 className="text-2xl font-semibold tracking-tight">Workspace</h1>
        <p className="text-muted-foreground text-sm text-pretty">
          Authorization infrastructure is active. Clinical modules are intentionally unavailable in
          this phase.
        </p>
      </div>
      <SessionHome />
      <AuthFormMessage
        variant="success"
        title="Protected area"
        description="This layout is guarded by session checks and role-aware navigation."
      />
    </div>
  );
}
