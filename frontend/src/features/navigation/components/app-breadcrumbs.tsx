'use client';

import { ChevronRight } from 'lucide-react';
import Link from 'next/link';

import { useBreadcrumbs } from '@/features/navigation/hooks/use-breadcrumbs';
import { cn } from '@/lib/utils';

type AppBreadcrumbsProps = {
  className?: string;
};

export function AppBreadcrumbs({ className }: AppBreadcrumbsProps) {
  const crumbs = useBreadcrumbs();

  return (
    <nav aria-label="Breadcrumb" className={cn('min-w-0', className)}>
      <ol className="text-muted-foreground flex flex-wrap items-center gap-1 text-sm">
        {crumbs.map((crumb, index) => {
          const isLast = index === crumbs.length - 1;
          return (
            <li key={`${crumb.label}-${index}`} className="flex items-center gap-1">
              {index > 0 ? <ChevronRight className="size-3.5 opacity-60" aria-hidden /> : null}
              {crumb.href && !isLast ? (
                <Link
                  href={crumb.href}
                  className="hover:text-foreground truncate transition-colors focus-visible:ring-2 focus-visible:ring-ring/50 focus-visible:outline-none"
                >
                  {crumb.label}
                </Link>
              ) : (
                <span
                  className={cn('truncate', isLast && 'text-foreground font-medium')}
                  aria-current={isLast ? 'page' : undefined}
                >
                  {crumb.label}
                </span>
              )}
            </li>
          );
        })}
      </ol>
    </nav>
  );
}
