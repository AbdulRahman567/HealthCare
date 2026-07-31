'use client';

import { Check, X } from 'lucide-react';
import Link from 'next/link';

import { Badge } from '@/components/ui/badge';
import { buttonVariants } from '@/components/ui/button';
import { Card, CardContent, CardFooter, CardHeader } from '@/components/ui/card';
import {
  type BillingInterval,
  type Plan,
  formatPrice,
  getIntervalLabel,
} from '@/features/pricing/data/plans';
import { cn } from '@/lib/utils';

type PricingCardProps = {
  plan: Plan;
  interval: BillingInterval;
};

export function PricingCard({ plan, interval }: PricingCardProps) {
  const price = interval === 'monthly' ? plan.pricing.monthly : plan.pricing.yearly;
  const isFree = price === 0;
  const isCustom = price === null;
  const isPopular = plan.popular;

  return (
    <Card
      className={cn(
        'relative flex flex-col transition-shadow duration-200 hover:shadow-md',
        isPopular && 'ring-2 ring-primary shadow-md',
      )}
    >
      {isPopular ? (
        <Badge className="absolute -top-3 left-1/2 -translate-x-1/2 px-4 py-1 text-xs font-semibold uppercase tracking-wider">
          Most popular
        </Badge>
      ) : null}

      <CardHeader className="gap-4 pb-4">
        <div>
          <h3 className="text-lg font-semibold">{plan.name}</h3>
          <p className="mt-1 text-sm text-muted-foreground">{plan.description}</p>
        </div>

        <div className="flex items-baseline gap-1">
          {isCustom ? (
            <span className="text-4xl font-bold tracking-tight">Custom</span>
          ) : (
            <>
              <span className="text-4xl font-bold tracking-tight">{formatPrice(price)}</span>
              {!isFree ? (
                <span className="text-sm text-muted-foreground">{getIntervalLabel(interval)}</span>
              ) : null}
            </>
          )}
        </div>

        {plan.trialDays ? (
          <p className="text-xs text-muted-foreground">
            <span className="font-medium text-foreground">{plan.trialDays}-day free trial</span>
            {' — '}no credit card required
          </p>
        ) : null}
      </CardHeader>

      <CardContent className="flex-1">
        <Link
          href={plan.cta.href}
          className={cn(buttonVariants({ variant: isPopular ? 'default' : 'outline' }), 'w-full')}
        >
          {plan.cta.label}
        </Link>
      </CardContent>

      <CardFooter className="flex-col items-stretch gap-3 border-t pt-4">
        <p className="text-xs font-medium text-muted-foreground uppercase tracking-wider">
          What&apos;s included
        </p>
        <ul className="space-y-2.5">
          {plan.features.map((feature) => (
            <li key={feature.text} className="flex items-start gap-2.5 text-sm">
              {feature.included ? (
                <Check className="mt-0.5 size-4 shrink-0 text-emerald-500" />
              ) : (
                <X className="mt-0.5 size-4 shrink-0 text-muted-foreground/40" />
              )}
              <span className={cn(feature.included ? '' : 'text-muted-foreground/50')}>
                {feature.text}
              </span>
            </li>
          ))}
        </ul>
      </CardFooter>
    </Card>
  );
}
