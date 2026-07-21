'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import type { ReactNode } from 'react';

import { buttonVariants } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';
import {
  APP_NAVIGATION,
  canAccessNavItem,
} from '@/features/auth/config/navigation';
import { cn } from '@/lib/utils';
import { useSession } from '@/providers/session-provider';

type RoleBasedNavProps = {
  className?: string;
};

export function RoleBasedNav({ className }: RoleBasedNavProps) {
  const pathname = usePathname();
  const { user } = useSession();

  const roles = user?.roles ?? [];
  const permissions = user?.permissions ?? [];
  const items = APP_NAVIGATION.filter((item) => canAccessNavItem(item, roles, permissions));

  return (
    <nav className={cn('flex flex-col gap-1', className)} aria-label="Application">
      {items.map((item) => {
        const active = pathname === item.href;
        if (item.comingSoon) {
          return (
            <div
              key={item.id}
              className="text-muted-foreground flex items-center justify-between rounded-lg px-3 py-2 text-sm"
              title={item.description}
            >
              <span>{item.label}</span>
              <span className="bg-muted rounded px-1.5 py-0.5 text-[10px] font-medium tracking-wide uppercase">
                Soon
              </span>
            </div>
          );
        }

        return (
          <Link
            key={item.id}
            href={item.href}
            className={cn(
              'rounded-lg px-3 py-2 text-sm transition-colors',
              active
                ? 'bg-primary/10 text-primary font-medium'
                : 'text-muted-foreground hover:bg-muted hover:text-foreground',
            )}
          >
            {item.label}
          </Link>
        );
      })}
    </nav>
  );
}

type ProtectedShellProps = {
  children: ReactNode;
};

export function ProtectedShell({ children }: ProtectedShellProps) {
  const { user, signOut } = useSession();

  return (
    <div className="bg-background min-h-screen md:grid md:grid-cols-[240px_1fr]">
      <aside className="border-border/80 hidden border-r md:flex md:flex-col md:gap-6 md:p-5">
        <div className="space-y-1">
          <p className="text-sm font-semibold tracking-wide">Healthcare HMS</p>
          <p className="text-muted-foreground text-xs">Authorized workspace</p>
        </div>
        <RoleBasedNav />
        <div className="mt-auto space-y-3">
          <Separator />
          <div className="space-y-1">
            <p className="text-sm font-medium">
              {user?.firstName} {user?.lastName}
            </p>
            <p className="text-muted-foreground truncate text-xs">{user?.email}</p>
            <p className="text-muted-foreground text-[11px]">{user?.roles.join(', ')}</p>
          </div>
          <button
            type="button"
            className={cn(buttonVariants({ variant: 'outline' }), 'h-9 w-full')}
            onClick={() => void signOut()}
          >
            Sign out
          </button>
        </div>
      </aside>

      <div className="flex min-h-screen flex-col">
        <header className="border-border/80 flex items-center justify-between border-b px-4 py-3 md:hidden">
          <div>
            <p className="text-sm font-semibold">Healthcare HMS</p>
            <p className="text-muted-foreground text-xs">{user?.email}</p>
          </div>
          <button
            type="button"
            className={cn(buttonVariants({ variant: 'outline', size: 'sm' }), 'h-8')}
            onClick={() => void signOut()}
          >
            Sign out
          </button>
        </header>
        <div className="border-border/80 overflow-x-auto border-b px-4 py-2 md:hidden">
          <RoleBasedNav className="flex-row gap-2" />
        </div>
        <main className="flex-1 px-4 py-6 sm:px-6 lg:px-8">{children}</main>
      </div>
    </div>
  );
}
