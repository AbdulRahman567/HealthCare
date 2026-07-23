'use client';

import { useSession } from '@/providers/session-provider';

/**
 * Compact signed-in summary for the dashboard.
 */
export function WorkspaceHome() {
  const { user } = useSession();

  if (!user) {
    return null;
  }

  return (
    <section className="rounded-xl border p-5">
      <h2 className="font-medium">
        Welcome, {user.firstName} {user.lastName}
      </h2>
      <p className="text-muted-foreground mt-1 text-sm">{user.email}</p>
      {user.tenantId ? (
        <p className="text-muted-foreground mt-3 text-xs">Hospital workspace · {user.tenantId}</p>
      ) : (
        <p className="text-muted-foreground mt-3 text-xs">Platform workspace</p>
      )}
    </section>
  );
}
