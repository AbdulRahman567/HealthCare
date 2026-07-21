'use client';

import { useSession } from '@/providers/session-provider';

export function ProfilePanel() {
  const { user } = useSession();

  if (!user) {
    return null;
  }

  return (
    <div className="space-y-4 rounded-xl border p-5 text-sm">
      <div className="grid gap-3 sm:grid-cols-2">
        <div>
          <p className="text-muted-foreground">First name</p>
          <p className="font-medium">{user.firstName}</p>
        </div>
        <div>
          <p className="text-muted-foreground">Last name</p>
          <p className="font-medium">{user.lastName}</p>
        </div>
        <div className="sm:col-span-2">
          <p className="text-muted-foreground">Email</p>
          <p className="font-medium">{user.email}</p>
        </div>
        <div>
          <p className="text-muted-foreground">Phone</p>
          <p className="font-medium">{user.phone ?? '—'}</p>
        </div>
        <div>
          <p className="text-muted-foreground">Status</p>
          <p className="font-medium">{user.status}</p>
        </div>
      </div>
      <div>
        <p className="text-muted-foreground mb-2">Roles</p>
        <div className="flex flex-wrap gap-2">
          {user.roles.map((role) => (
            <span key={role} className="bg-muted rounded-md px-2 py-1 text-xs font-medium">
              {role}
            </span>
          ))}
        </div>
      </div>
    </div>
  );
}
