import { ArrowRight, HeartPulse, ShieldCheck } from 'lucide-react';
import Link from 'next/link';

import { buttonVariants } from '@/components/ui/button';
import { cn } from '@/lib/utils';

const perks = [
  { icon: ShieldCheck, text: 'No credit card required' },
  { icon: HeartPulse, text: 'Free Basic plan available' },
  { icon: ArrowRight, text: 'Cancel anytime' },
];

export function CtaSection() {
  return (
    <section className="relative overflow-hidden rounded-2xl border border-primary/10 bg-gradient-to-br from-primary/[0.04] via-primary/[0.02] to-primary/[0.06] p-8 md:p-14">
      {/* Decorative elements */}
      <div className="pointer-events-none absolute -right-20 -top-20 size-64 rounded-full bg-primary/[0.04] blur-3xl" />
      <div className="pointer-events-none absolute -bottom-16 -left-16 size-48 rounded-full bg-primary/[0.03] blur-3xl" />

      <div className="relative mx-auto max-w-2xl text-center">
        {/* Badge */}
        <span className="inline-flex items-center gap-1.5 rounded-full border border-primary/15 bg-primary/[0.06] px-3.5 py-1 text-xs font-medium text-primary">
          Get started today
        </span>

        {/* Headline */}
        <h2 className="mt-4 text-3xl font-semibold tracking-tight text-balance md:text-4xl">
          Ready to transform your hospital operations?
        </h2>
        <p className="text-muted-foreground mx-auto mt-3 max-w-lg text-base text-pretty">
          Join hundreds of healthcare facilities already using Healthcare HMS. Start your free trial
          today — no credit card required.
        </p>

        {/* CTAs */}
        <div className="mt-8 flex flex-col items-center justify-center gap-3 sm:flex-row">
          <Link
            href="/register/hospital?plan=STANDARD"
            className={cn(
              buttonVariants({ size: 'lg' }),
              'group h-12 min-w-52 gap-2 rounded-xl px-6 text-base font-semibold shadow-sm transition-all duration-200 hover:shadow-md',
            )}
          >
            Start your 14-day free trial
            <ArrowRight className="size-4 transition-transform duration-200 group-hover:translate-x-0.5" />
          </Link>
          <Link
            href="/register/hospital?plan=ENTERPRISE"
            className={cn(
              buttonVariants({ variant: 'outline', size: 'lg' }),
              'h-12 min-w-44 rounded-xl px-6 text-base font-medium',
            )}
          >
            Talk to sales
          </Link>
        </div>

        {/* Perk list */}
        <div className="mt-6 flex flex-wrap items-center justify-center gap-x-6 gap-y-2">
          {perks.map((perk) => {
            const Icon = perk.icon;
            return (
              <div
                key={perk.text}
                className="flex items-center gap-1.5 text-xs text-muted-foreground"
              >
                <Icon className="size-3.5 text-primary/70" />
                {perk.text}
              </div>
            );
          })}
        </div>
      </div>
    </section>
  );
}
