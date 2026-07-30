'use client';

import { type BillingInterval } from '@/features/pricing/data/plans';
import { cn } from '@/lib/utils';

type PricingToggleProps = {
  interval: BillingInterval;
  onChange: (interval: BillingInterval) => void;
};

export function PricingToggle({ interval, onChange }: PricingToggleProps) {
  return (
    <div className="inline-flex items-center gap-3 rounded-full border bg-white/60 p-1 shadow-xs">
      <button
        type="button"
        onClick={() => onChange('monthly')}
        className={cn(
          'rounded-full px-4 py-2 text-sm font-medium transition-all',
          interval === 'monthly'
            ? 'bg-primary text-primary-foreground shadow-sm'
            : 'text-muted-foreground hover:text-foreground',
        )}
      >
        Monthly
      </button>
      <button
        type="button"
        onClick={() => onChange('yearly')}
        className={cn(
          'rounded-full px-4 py-2 text-sm font-medium transition-all',
          interval === 'yearly'
            ? 'bg-primary text-primary-foreground shadow-sm'
            : 'text-muted-foreground hover:text-foreground',
        )}
      >
        Yearly
        <span className="ml-1.5 rounded-full bg-emerald-100 px-2 py-0.5 text-[10px] font-semibold text-emerald-700">
          Save up to 17%
        </span>
      </button>
    </div>
  );
}
