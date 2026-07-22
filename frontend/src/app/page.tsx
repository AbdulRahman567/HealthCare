import type { Metadata } from 'next';
import Link from 'next/link';
import { Building2, LockKeyhole, ShieldCheck } from 'lucide-react';

import { buttonVariants } from '@/components/ui/button';
import { cn } from '@/lib/utils';

export const metadata: Metadata = {
  title: 'Healthcare HMS',
  description: 'Enterprise multi-tenant Healthcare Management System',
};

export default function LandingPage() {
  return (
    <div className="relative min-h-screen overflow-hidden bg-[linear-gradient(180deg,#f4f8fb_0%,#ffffff_42%,#eef6fa_100%)]">
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(15,95,138,0.12),transparent_40%),radial-gradient(circle_at_bottom_left,rgba(11,58,91,0.08),transparent_35%)]" />

      <header className="relative mx-auto flex w-full max-w-6xl items-center justify-between px-6 py-6">
        <div className="flex items-center gap-2">
          <span className="bg-primary/10 text-primary flex size-9 items-center justify-center rounded-lg">
            <ShieldCheck className="size-4" aria-hidden />
          </span>
          <span className="text-sm font-semibold tracking-wide">Healthcare HMS</span>
        </div>
        <nav className="flex items-center gap-3">
          <Link href="/login" className={cn(buttonVariants({ variant: 'ghost' }), 'h-9 px-3')}>
            Sign in
          </Link>
          <Link
            href="/register/hospital"
            className={cn(buttonVariants({ variant: 'default' }), 'h-9 px-3')}
          >
            Register hospital
          </Link>
        </nav>
      </header>

      <main className="relative mx-auto flex w-full max-w-6xl flex-col gap-16 px-6 pb-20 pt-10 md:pt-16">
        <section className="mx-auto max-w-3xl space-y-6 text-center">
          <p className="text-primary text-xs font-semibold tracking-[0.18em] uppercase">
            Healthcare operations platform
          </p>
          <h1 className="text-4xl font-semibold tracking-tight text-balance md:text-5xl lg:text-6xl">
            Healthcare HMS
          </h1>
          <p className="text-muted-foreground mx-auto max-w-2xl text-base text-pretty md:text-lg">
            Secure multi-tenant access for hospitals. Register your facility, authenticate your
            administrators, and prepare for clinical workflows — without clutter or premature
            dashboards.
          </p>
          <div className="flex flex-col items-center justify-center gap-3 sm:flex-row">
            <Link
              href="/register/hospital"
              className={cn(buttonVariants({ size: 'lg' }), 'h-11 min-w-44 px-5')}
            >
              Register your hospital
            </Link>
            <Link
              href="/login"
              className={cn(
                buttonVariants({ variant: 'outline', size: 'lg' }),
                'h-11 min-w-44 px-5',
              )}
            >
              Sign in
            </Link>
          </div>
        </section>

        <section className="grid gap-6 md:grid-cols-3">
          <article className="space-y-3 rounded-2xl border border-black/5 bg-white/80 p-6 shadow-sm backdrop-blur">
            <Building2 className="text-primary size-5" />
            <h2 className="font-medium">Hospital onboarding</h2>
            <p className="text-muted-foreground text-sm leading-relaxed">
              Create a tenant for your hospital and establish the foundation for staff access.
            </p>
          </article>
          <article className="space-y-3 rounded-2xl border border-black/5 bg-white/80 p-6 shadow-sm backdrop-blur">
            <LockKeyhole className="text-primary size-5" />
            <h2 className="font-medium">Secure authentication</h2>
            <p className="text-muted-foreground text-sm leading-relaxed">
              JWT-backed sign-in with validated forms, clear errors, and production-ready API
              wiring.
            </p>
          </article>
          <article className="space-y-3 rounded-2xl border border-black/5 bg-white/80 p-6 shadow-sm backdrop-blur">
            <ShieldCheck className="text-primary size-5" />
            <h2 className="font-medium">Enterprise ready</h2>
            <p className="text-muted-foreground text-sm leading-relaxed">
              Built for tenant isolation, auditability, and the clinical modules that follow.
            </p>
          </article>
        </section>
      </main>
    </div>
  );
}
