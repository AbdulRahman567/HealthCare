'use client';

import { Check, HeartPulse, Rocket, Shield, Sparkles } from 'lucide-react';
import Link from 'next/link';

import { buttonVariants } from '@/components/ui/button';
import { cn } from '@/lib/utils';

const planHighlights = [
  {
    name: 'Basic',
    price: 'Free',
    description: 'For small clinics getting started',
    icon: HeartPulse,
    features: ['Up to 10 staff', 'Basic patient records', 'Appointment scheduling'],
    color: 'text-muted-foreground',
    borderColor: 'border-black/[0.06]',
  },
  {
    name: 'Standard',
    price: '$29',
    period: '/month',
    description: 'For growing hospitals',
    icon: Rocket,
    features: ['Unlimited staff', 'Clinical workflows', 'Billing & insurance', 'Analytics'],
    color: 'text-primary',
    borderColor: 'border-primary/20',
    popular: true,
  },
  {
    name: 'Enterprise',
    price: 'Custom',
    description: 'For large healthcare systems',
    icon: Shield,
    features: ['White-label', 'Dedicated support', 'Custom integrations', 'SLA guarantee'],
    color: 'text-muted-foreground',
    borderColor: 'border-black/[0.06]',
  },
];

export function PricingTeaserSection() {
  return (
    <section className="space-y-10">
      {/* Header */}
      <div className="mx-auto max-w-2xl text-center">
        <span className="inline-flex items-center gap-1.5 rounded-full border border-primary/15 bg-primary/[0.04] px-3.5 py-1 text-xs font-medium text-primary">
          Simple, transparent pricing
        </span>
        <h2 className="mt-4 text-3xl font-semibold tracking-tight text-balance md:text-4xl">
          Start for free, scale as you grow
        </h2>
        <p className="text-muted-foreground mx-auto mt-3 max-w-xl text-sm text-pretty">
          From our free Basic plan to Premium for growing hospitals and Enterprise for large
          healthcare systems. All paid plans include a{' '}
          <span className="font-medium text-foreground">14-day free trial</span> — no credit card
          required.
        </p>
      </div>

      {/* Plan cards */}
      <div className="grid gap-5 md:grid-cols-3">
        {planHighlights.map((plan) => {
          const Icon = plan.icon;
          return (
            <div
              key={plan.name}
              className={cn(
                'group relative rounded-2xl border bg-white/70 p-6 shadow-xs transition-all duration-300 hover:shadow-md',
                plan.borderColor,
                plan.popular && 'ring-2 ring-primary/10',
              )}
            >
              {/* Popular badge */}
              {plan.popular && (
                <span className="absolute -top-2.5 left-1/2 -translate-x-1/2 inline-flex items-center gap-1 rounded-full bg-primary px-3 py-0.5 text-[11px] font-semibold text-primary-foreground shadow-sm">
                  <Sparkles className="size-3" />
                  Most popular
                </span>
              )}

              {/* Icon */}
              <span
                className={cn(
                  'flex size-10 items-center justify-center rounded-xl bg-primary/[0.07]',
                  plan.color,
                )}
              >
                <Icon className="size-5" />
              </span>

              {/* Name & price */}
              <div className="mt-4 space-y-1">
                <h3 className="text-lg font-semibold">{plan.name}</h3>
                <div className="flex items-baseline gap-0.5">
                  <span className="text-3xl font-bold tracking-tight">{plan.price}</span>
                  {plan.period && (
                    <span className="text-sm text-muted-foreground">{plan.period}</span>
                  )}
                </div>
                <p className="text-sm text-muted-foreground">{plan.description}</p>
              </div>

              {/* Features */}
              <ul className="mt-5 space-y-2.5 border-t border-black/[0.04] pt-5">
                {plan.features.map((feat) => (
                  <li key={feat} className="flex items-center gap-2 text-sm">
                    <Check className="size-3.5 shrink-0 text-emerald-500" />
                    <span className="text-muted-foreground">{feat}</span>
                  </li>
                ))}
              </ul>

              {/* CTA */}
              <div className="mt-6">
                <Link
                  href={
                    plan.popular
                      ? '/register/hospital?plan=STANDARD'
                      : plan.name === 'Enterprise'
                        ? '/register/hospital?plan=ENTERPRISE'
                        : '/register/hospital'
                  }
                  className={cn(
                    buttonVariants({
                      variant: plan.popular ? 'default' : 'outline',
                    }),
                    'w-full rounded-xl',
                  )}
                >
                  {plan.price === 'Free' ? 'Get started free' : 'Start free trial'}
                </Link>
              </div>
            </div>
          );
        })}
      </div>

      {/* Bottom CTA */}
      <div className="text-center">
        <Link
          href="/pricing"
          className="text-sm font-medium text-primary underline-offset-4 hover:underline"
        >
          Compare all plans and features &rarr;
        </Link>
      </div>
    </section>
  );
}
