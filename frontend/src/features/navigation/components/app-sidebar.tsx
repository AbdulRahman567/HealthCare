'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';

import { Separator } from '@/components/ui/separator';
import { NAV_SECTION_LABELS } from '@/features/navigation/config/nav-items';
import { useWorkspaceNavigation } from '@/features/navigation/hooks/use-workspace-navigation';
import { resolveNavIcon } from '@/features/navigation/lib/icons';
import { isNavItemActive } from '@/features/navigation/lib/navigation';
import { cn } from '@/lib/utils';

type AppSidebarProps = {
  className?: string;
  onNavigate?: () => void;
};

export function AppSidebar({ className, onNavigate }: AppSidebarProps) {
  const pathname = usePathname();
  const { sidebarGroups } = useWorkspaceNavigation();

  return (
    <aside
      className={cn(
        'bg-sidebar text-sidebar-foreground flex h-full flex-col gap-6 border-r border-sidebar-border',
        className,
      )}
      aria-label="Primary"
    >
      <div className="px-5 pt-5">
        <Link
          href="/app"
          className="block space-y-0.5 focus-visible:ring-2 focus-visible:ring-ring/50 focus-visible:outline-none"
          onClick={onNavigate}
        >
          <p className="text-sm font-semibold tracking-wide">Healthcare HMS</p>
          <p className="text-muted-foreground text-xs">Authorized workspace</p>
        </Link>
      </div>

      <nav className="flex-1 space-y-5 overflow-y-auto px-3 pb-4" aria-label="Application">
        {sidebarGroups.map((group) => (
          <div key={group.section} className="space-y-1">
            <p className="text-muted-foreground px-2 text-[11px] font-medium tracking-wide uppercase">
              {NAV_SECTION_LABELS[group.section]}
            </p>
            <ul className="space-y-0.5">
              {group.items.map((item) => {
                const Icon = resolveNavIcon(item.icon);
                const active = isNavItemActive(pathname, item.href);

                return (
                  <li key={item.id}>
                    <Link
                      href={item.href}
                      onClick={onNavigate}
                      title={item.description}
                      aria-current={active ? 'page' : undefined}
                      className={cn(
                        'flex items-center gap-2.5 rounded-lg px-2.5 py-2 text-sm transition-colors focus-visible:ring-2 focus-visible:ring-ring/50 focus-visible:outline-none',
                        active
                          ? 'bg-sidebar-accent text-sidebar-accent-foreground font-medium'
                          : 'text-muted-foreground hover:bg-sidebar-accent/70 hover:text-foreground',
                        item.comingSoon && 'opacity-75',
                      )}
                    >
                      <Icon className="size-4 shrink-0 opacity-80" aria-hidden />
                      <span className="min-w-0 flex-1 truncate">{item.label}</span>
                      {item.comingSoon ? (
                        <span className="bg-muted text-muted-foreground rounded px-1.5 py-0.5 text-[10px] font-medium tracking-wide uppercase">
                          Soon
                        </span>
                      ) : null}
                    </Link>
                  </li>
                );
              })}
            </ul>
          </div>
        ))}
      </nav>

      <div className="mt-auto space-y-3 px-5 pb-5">
        <Separator />
        <p className="text-muted-foreground text-[11px] leading-relaxed">
          Menus reflect your granted permissions.
        </p>
      </div>
    </aside>
  );
}
