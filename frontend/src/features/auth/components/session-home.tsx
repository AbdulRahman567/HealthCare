'use client';

import { useSession } from '@/providers/session-provider';

export function SessionHome() {
  const { user, hasPermission } = useSession();

  if (!user) {
    return null;
  }

  return (
    <section className="space-y-4 rounded-xl border p-5">
      <div>
        <h2 className="font-medium">
          Signed in as {user.firstName} {user.lastName}
        </h2>
        <p className="text-muted-foreground mt-1 text-sm">{user.email}</p>
      </div>
      <dl className="grid gap-3 text-sm sm:grid-cols-2">
        <div>
          <dt className="text-muted-foreground">Roles</dt>
          <dd className="mt-1 font-medium">{user.roles.join(', ') || 'None'}</dd>
        </div>
        <div>
          <dt className="text-muted-foreground">Tenant</dt>
          <dd className="mt-1 font-medium">{user.tenantId ?? 'Platform'}</dd>
        </div>
        <div className="sm:col-span-2">
          <dt className="text-muted-foreground">Permissions loaded</dt>
          <dd className="mt-1 font-medium">{user.permissions.length}</dd>
        </div>
        <div className="sm:col-span-2">
          <dt className="text-muted-foreground">Hospital read access</dt>
          <dd className="mt-1 font-medium">
            {hasPermission('HOSPITAL_READ') ? 'Granted' : 'Denied'}
          </dd>
        </div>
      </dl>
    </section>
  );
}
