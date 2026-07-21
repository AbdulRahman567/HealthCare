import Link from 'next/link';
import type { ReactNode } from 'react';

import { HeartPulse } from 'lucide-react';

import { Separator } from '@/components/ui/separator';
import { cn } from '@/lib/utils';

type AuthShellProps = {
  title: string;
  description: string;
  children: ReactNode;
  footer?: ReactNode;
  className?: string;
};

/**
 * Professional split-panel authentication shell used by login/register flows.
 */
export function AuthShell({ title, description, children, footer, className }: AuthShellProps) {
  return (
    <div className="bg-background grid min-h-screen lg:grid-cols-2">
      <aside className="relative hidden overflow-hidden bg-[linear-gradient(145deg,#0b3a5b_0%,#0f5f8a_48%,#1a7a9c_100%)] px-10 py-12 text-white lg:flex lg:flex-col lg:justify-between">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_20%_20%,rgba(255,255,255,0.14),transparent_45%),radial-gradient(circle_at_80%_70%,rgba(56,189,248,0.2),transparent_40%)]" />
        <div className="relative space-y-8">
          <Link href="/" className="inline-flex items-center gap-2 text-sm font-medium tracking-wide">
            <span className="bg-white/15 flex size-9 items-center justify-center rounded-lg backdrop-blur-sm">
              <HeartPulse className="size-4" aria-hidden />
            </span>
            Healthcare HMS
          </Link>
          <div className="max-w-md space-y-4">
            <h1 className="text-3xl font-semibold tracking-tight text-balance xl:text-4xl">
              Secure access for modern hospital operations
            </h1>
            <p className="text-white/80 text-sm leading-relaxed text-pretty xl:text-base">
              Multi-tenant authentication built for clinical teams — clear, auditable, and ready for
              enterprise healthcare workflows.
            </p>
          </div>
        </div>
        <div className="relative space-y-3 text-sm text-white/75">
          <p>JWT-backed access · Tenant isolation · Role-ready security</p>
          <Separator className="bg-white/20" />
          <p className="text-xs text-white/60">© {new Date().getFullYear()} Healthcare HMS</p>
        </div>
      </aside>

      <main className="flex items-center justify-center px-4 py-10 sm:px-8">
        <div className={cn('w-full max-w-md space-y-6', className)}>
          <div className="space-y-2 lg:hidden">
            <Link href="/" className="text-foreground inline-flex items-center gap-2 text-sm font-medium">
              <span className="bg-primary/10 text-primary flex size-8 items-center justify-center rounded-lg">
                <HeartPulse className="size-3.5" aria-hidden />
              </span>
              Healthcare HMS
            </Link>
          </div>

          <div className="space-y-2">
            <h2 className="text-2xl font-semibold tracking-tight">{title}</h2>
            <p className="text-muted-foreground text-sm text-pretty">{description}</p>
          </div>

          {children}

          {footer ? <div className="text-muted-foreground text-sm">{footer}</div> : null}
        </div>
      </main>
    </div>
  );
}
