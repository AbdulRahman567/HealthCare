'use client';

import Link from 'next/link';

import { buttonVariants } from '@/components/ui/button';
import { useQuickActions } from '@/features/navigation/hooks/use-workspace-navigation';
import { resolveNavIcon } from '@/features/navigation/lib/icons';
import { cn } from '@/lib/utils';

type QuickActionsProps = {
  className?: string;
};

export function QuickActions({ className }: QuickActionsProps) {
  const actions = useQuickActions();

  if (actions.length === 0) {
    return (
      <section className={cn('space-y-2', className)} aria-labelledby="quick-actions-heading">
        <h2 id="quick-actions-heading" className="text-base font-semibold tracking-tight">
          Quick actions
        </h2>
        <p className="text-muted-foreground text-sm">
          No create or update actions are available for your permissions.
        </p>
      </section>
    );
  }

  return (
    <section className={cn('space-y-3', className)} aria-labelledby="quick-actions-heading">
      <div>
        <h2 id="quick-actions-heading" className="text-base font-semibold tracking-tight">
          Quick actions
        </h2>
        <p className="text-muted-foreground text-sm">Common tasks available with your access.</p>
      </div>
      <ul className="flex flex-wrap gap-2">
        {actions.map((action) => {
          const Icon = resolveNavIcon(action.icon);
          return (
            <li key={action.id}>
              <Link
                href={action.href}
                title={action.description}
                aria-label={
                  action.comingSoon ? `${action.label} (coming soon)` : action.label
                }
                className={cn(
                  buttonVariants({ variant: 'outline' }),
                  'h-9 gap-2',
                  action.comingSoon && 'opacity-80',
                )}
              >
                <Icon className="size-4" aria-hidden />
                <span>{action.label}</span>
                {action.comingSoon ? (
                  <span className="text-muted-foreground text-[10px] font-medium tracking-wide uppercase">
                    Soon
                  </span>
                ) : null}
              </Link>
            </li>
          );
        })}
      </ul>
    </section>
  );
}
