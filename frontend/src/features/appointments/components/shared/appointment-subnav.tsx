'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';

import { cn } from '@/lib/utils';

const LINKS: ReadonlyArray<{ href: string; label: string; exact?: boolean }> = [
  { href: '/app/appointments', label: 'Dashboard', exact: true },
  { href: '/app/appointments/list', label: 'List' },
  { href: '/app/appointments/calendar', label: 'Calendar' },
  { href: '/app/appointments/queue', label: 'Queue' },
  { href: '/app/appointments/availability', label: 'Availability' },
];

export function AppointmentSubnav() {
  const pathname = usePathname();

  return (
    <nav
      aria-label="Appointment views"
      className="flex flex-wrap gap-1 rounded-lg border bg-muted/40 p-1"
    >
      {LINKS.map((link) => {
        const active = link.exact
          ? pathname === link.href
          : pathname === link.href || pathname.startsWith(`${link.href}/`);
        return (
          <Link
            key={link.href}
            href={link.href}
            className={cn(
              'rounded-md px-3 py-1.5 text-sm font-medium transition-colors',
              active
                ? 'bg-background text-foreground shadow-sm'
                : 'text-muted-foreground hover:text-foreground',
            )}
          >
            {link.label}
          </Link>
        );
      })}
    </nav>
  );
}
