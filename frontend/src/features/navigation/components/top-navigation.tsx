'use client';

import { Menu, X } from 'lucide-react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import type { RefObject } from 'react';

import { Button, buttonVariants } from '@/components/ui/button';
import { AppBreadcrumbs } from '@/features/navigation/components/app-breadcrumbs';
import {
  useQuickActions,
  useWorkspaceNavigation,
} from '@/features/navigation/hooks/use-workspace-navigation';
import { resolveNavIcon } from '@/features/navigation/lib/icons';
import { isNavItemActive } from '@/features/navigation/lib/navigation';
import { cn } from '@/lib/utils';
import { useSession } from '@/providers/session-provider';

type TopNavigationProps = {
  mobileNavOpen: boolean;
  onToggleMobileNav: () => void;
  menuButtonRef?: RefObject<HTMLButtonElement | null>;
};

export function TopNavigation({
  mobileNavOpen,
  onToggleMobileNav,
  menuButtonRef,
}: TopNavigationProps) {
  const pathname = usePathname();
  const { user, signOut } = useSession();
  const { topItems } = useWorkspaceNavigation();
  const quickActions = useQuickActions();
  const primaryAction = quickActions[0];
  const PrimaryIcon = primaryAction ? resolveNavIcon(primaryAction.icon) : null;

  return (
    <header className="border-border/80 bg-background/95 sticky top-0 z-40 border-b backdrop-blur">
      <div className="flex items-center gap-3 px-4 py-3 sm:px-6">
        <Button
          ref={menuButtonRef}
          type="button"
          variant="outline"
          size="icon-sm"
          className="md:hidden"
          onClick={onToggleMobileNav}
          aria-expanded={mobileNavOpen}
          aria-controls="mobile-sidebar"
          aria-label={mobileNavOpen ? 'Close navigation' : 'Open navigation'}
        >
          {mobileNavOpen ? <X /> : <Menu />}
        </Button>

        <div className="min-w-0 flex-1 space-y-1">
          <AppBreadcrumbs />
          <p className="text-muted-foreground hidden truncate text-xs sm:block">
            {user ? `${user.firstName} ${user.lastName}` : 'Signed in'}
            {user?.email ? ` · ${user.email}` : null}
          </p>
        </div>

        {primaryAction && PrimaryIcon ? (
          <Link
            href={primaryAction.href}
            title={primaryAction.description}
            className={cn(
              buttonVariants({ variant: 'outline', size: 'sm' }),
              'hidden h-8 lg:inline-flex',
              primaryAction.comingSoon && 'opacity-80',
            )}
          >
            <PrimaryIcon data-icon="inline-start" aria-hidden />
            {primaryAction.label}
          </Link>
        ) : null}

        <Button
          type="button"
          variant="outline"
          size="sm"
          className="h-8 shrink-0"
          onClick={() => void signOut()}
        >
          Sign out
        </Button>
      </div>

      <div className="border-border/60 overflow-x-auto border-t md:hidden">
        <nav className="flex gap-1 px-4 py-2" aria-label="Module shortcuts">
          {topItems.map((item) => {
            const Icon = resolveNavIcon(item.icon);
            const active = isNavItemActive(pathname, item.href);
            return (
              <Link
                key={item.id}
                href={item.href}
                aria-current={active ? 'page' : undefined}
                className={cn(
                  'inline-flex shrink-0 items-center gap-1.5 rounded-lg px-2.5 py-1.5 text-xs transition-colors focus-visible:ring-2 focus-visible:ring-ring/50 focus-visible:outline-none',
                  active
                    ? 'bg-primary/10 text-primary font-medium'
                    : 'text-muted-foreground hover:bg-muted hover:text-foreground',
                  item.comingSoon && 'opacity-80',
                )}
              >
                <Icon className="size-3.5" aria-hidden />
                {item.label}
              </Link>
            );
          })}
        </nav>
      </div>
    </header>
  );
}
