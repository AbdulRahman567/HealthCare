'use client';

import { useState } from 'react';

import Link from 'next/link';

import { SiteHeader } from '@/components/layouts/site-header';
import { SiteFooter } from '@/components/layouts/site-footer';
import { buttonVariants } from '@/components/ui/button';
import { PricingCard } from '@/features/pricing/components/pricing-card';
import { PricingToggle } from '@/features/pricing/components/pricing-toggle';
import { PLANS, type BillingInterval } from '@/features/pricing/data/plans';
import { cn } from '@/lib/utils';

export default function PricingPage() {
  const [interval, setInterval] = useState<BillingInterval>('monthly');

  return (
    <div className="relative min-h-screen bg-[linear-gradient(180deg,#f4f8fb_0%,#ffffff_42%,#eef6fa_100%)]">
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(15,95,138,0.12),transparent_40%),radial-gradient(circle_at_bottom_left,rgba(11,58,91,0.08),transparent_35%)]" />

      <SiteHeader />

      <main className="relative mx-auto w-full max-w-6xl px-6 pb-20 pt-10 md:pt-16">
        {/* Hero */}
        <section className="mx-auto max-w-3xl text-center">
          <p className="text-primary text-xs font-semibold tracking-[0.18em] uppercase">Pricing</p>
          <h1 className="mt-3 text-4xl font-semibold tracking-tight text-balance md:text-5xl">
            Simple, transparent pricing
          </h1>
          <p className="text-muted-foreground mx-auto mt-4 max-w-2xl text-base text-pretty md:text-lg">
            Choose the plan that fits your healthcare organization. All plans include a{' '}
            <span className="font-medium text-foreground">14-day free trial</span> on paid tiers. No
            credit card required.
          </p>

          <div className="mt-8 flex justify-center">
            <PricingToggle interval={interval} onChange={setInterval} />
          </div>
        </section>

        {/* Plan cards */}
        <section className="mt-12 grid gap-6 md:grid-cols-2 lg:grid-cols-4">
          {PLANS.map((plan) => (
            <PricingCard key={plan.id} plan={plan} interval={interval} />
          ))}
        </section>

        {/* FAQ / CTA section */}
        <section className="mt-20 rounded-2xl border bg-white/60 p-8 text-center md:p-12">
          <h2 className="text-2xl font-semibold tracking-tight">
            Need a custom plan for your hospital group?
          </h2>
          <p className="text-muted-foreground mx-auto mt-3 max-w-xl text-sm text-pretty">
            We offer tailored solutions for large healthcare systems, multi-tenant hospital groups,
            and organizations with specific compliance or integration requirements.
          </p>
          <div className="mt-6 flex justify-center gap-4">
            <Link
              href="/register/hospital?plan=ENTERPRISE"
              className={cn(
                buttonVariants({ variant: 'default', size: 'lg' }),
                'h-11 min-w-44 px-5',
              )}
            >
              Contact sales
            </Link>
            <Link
              href="/register/hospital"
              className={cn(
                buttonVariants({ variant: 'outline', size: 'lg' }),
                'h-11 min-w-44 px-5',
              )}
            >
              Start with Basic
            </Link>
          </div>
        </section>
      </main>

      <SiteFooter />
    </div>
  );
}
