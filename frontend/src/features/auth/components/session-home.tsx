'use client';

import { Can } from '@/features/authorization/components/can';
import { Permissions } from '@/features/authorization/constants/permissions';
import { useAuthorization } from '@/features/authorization/hooks/use-authorization';
import { useSession } from '@/providers/session-provider';

export function SessionHome() {
  const { user } = useSession();
  const { permissions, can } = useAuthorization();

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
          <dd className="mt-1 font-medium">{permissions.length}</dd>
        </div>
        <div className="sm:col-span-2">
          <dt className="text-muted-foreground">Hospital read access</dt>
          <dd className="mt-1 font-medium">
            {can(Permissions.HOSPITAL_READ) ? 'Granted' : 'Denied'}
          </dd>
        </div>
      </dl>

      <Can permissions={[Permissions.HOSPITAL_UPDATE]}>
        <div className="bg-muted/40 rounded-lg border border-dashed p-3 text-sm">
          <p className="font-medium">Hospital settings</p>
          <p className="text-muted-foreground mt-1 text-xs">
            You can update hospital settings when that module ships.
          </p>
          <button
            type="button"
            className="bg-primary text-primary-foreground mt-3 rounded-md px-3 py-1.5 text-xs font-medium"
            disabled
          >
            Edit settings
          </button>
        </div>
      </Can>
    </section>
  );
}
