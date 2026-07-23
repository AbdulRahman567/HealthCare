'use client';

import Link from 'next/link';

import {
  Card,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { useDashboardCards } from '@/features/navigation/hooks/use-workspace-navigation';
import { resolveNavIcon } from '@/features/navigation/lib/icons';
import { cn } from '@/lib/utils';

type DashboardCardsProps = {
  className?: string;
};

export function DashboardCards({ className }: DashboardCardsProps) {
  const cards = useDashboardCards();

  if (cards.length === 0) {
    return (
      <section className={cn('space-y-2', className)} aria-labelledby="dashboard-modules-heading">
        <h2 id="dashboard-modules-heading" className="text-base font-semibold tracking-tight">
          Modules
        </h2>
        <p className="text-muted-foreground text-sm">
          No modules are available for your current permissions. Contact your hospital administrator
          if you need additional access.
        </p>
      </section>
    );
  }

  return (
    <section className={cn('space-y-3', className)} aria-labelledby="dashboard-modules-heading">
      <div>
        <h2 id="dashboard-modules-heading" className="text-base font-semibold tracking-tight">
          Modules
        </h2>
        <p className="text-muted-foreground text-sm">Open a module you are permitted to use.</p>
      </div>
      <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
        {cards.map((card) => {
          const Icon = resolveNavIcon(card.icon);
          return (
            <Link
              key={card.id}
              href={card.href}
              className="group block rounded-xl outline-none"
              aria-label={
                card.comingSoon ? `${card.title} (coming soon)` : card.title
              }
            >
              <Card
                className={cn(
                  'h-full transition-colors group-hover:bg-muted/40 group-focus-visible:ring-2 group-focus-visible:ring-ring',
                  card.comingSoon && 'opacity-80',
                )}
              >
                <CardHeader>
                  <div className="flex items-start justify-between gap-3">
                    <div className="bg-primary/10 text-primary flex size-9 items-center justify-center rounded-lg">
                      <Icon className="size-4" aria-hidden />
                    </div>
                    {card.comingSoon ? (
                      <span className="bg-muted text-muted-foreground rounded px-1.5 py-0.5 text-[10px] font-medium tracking-wide uppercase">
                        Soon
                      </span>
                    ) : null}
                  </div>
                  <CardTitle className="mt-2">{card.title}</CardTitle>
                  <CardDescription>{card.description}</CardDescription>
                </CardHeader>
              </Card>
            </Link>
          );
        })}
      </div>
    </section>
  );
}
