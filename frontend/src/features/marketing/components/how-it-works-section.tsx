import { Building2, Sparkles, UserPlus } from 'lucide-react';

import { cn } from '@/lib/utils';

const steps = [
  {
    icon: Building2,
    number: '01',
    title: 'Register your hospital',
    description:
      'Create your tenant and administrator account in under two minutes. No credit card required.',
    color: 'from-blue-500 to-blue-600',
    bgLight: 'bg-blue-50 dark:bg-blue-950/30',
    textColor: 'text-blue-600 dark:text-blue-400',
  },
  {
    icon: UserPlus,
    number: '02',
    title: 'Invite your team',
    description:
      'Add doctors, nurses, and staff with role-based access control. Each team member gets the right permissions instantly.',
    color: 'from-emerald-500 to-emerald-600',
    bgLight: 'bg-emerald-50 dark:bg-emerald-950/30',
    textColor: 'text-emerald-600 dark:text-emerald-400',
  },
  {
    icon: Sparkles,
    number: '03',
    title: 'Start providing care',
    description:
      'Manage patients, appointments, clinical notes, and billing from a single, integrated dashboard.',
    color: 'from-violet-500 to-violet-600',
    bgLight: 'bg-violet-50 dark:bg-violet-950/30',
    textColor: 'text-violet-600 dark:text-violet-400',
  },
];

export function HowItWorksSection() {
  return (
    <section className="space-y-10">
      {/* Header */}
      <div className="mx-auto max-w-2xl text-center">
        <span className="inline-flex items-center gap-1.5 rounded-full border border-primary/15 bg-primary/[0.04] px-3.5 py-1 text-xs font-medium text-primary">
          Get started
        </span>
        <h2 className="mt-4 text-3xl font-semibold tracking-tight text-balance md:text-4xl">
          Up and running in 3 simple steps
        </h2>
        <p className="text-muted-foreground mt-3 text-sm text-pretty">
          From zero to fully operational — no implementation team required.
        </p>
      </div>

      {/* Steps */}
      <div className="grid gap-8 md:grid-cols-3 md:gap-10">
        {steps.map((step, i) => {
          const Icon = step.icon;
          return (
            <div key={step.number} className="group relative">
              {/* Connector line (desktop) */}
              {i < steps.length - 1 && (
                <div className="absolute top-10 left-[calc(50%+2.5rem)] hidden h-px w-[calc(100%-5rem)] md:block">
                  <div className="h-full w-full border-t border-dashed border-primary/20" />
                </div>
              )}

              <div className="relative space-y-5">
                {/* Number + icon badge */}
                <div className="flex justify-center">
                  <div
                    className={cn(
                      'relative flex size-20 items-center justify-center rounded-2xl',
                      step.bgLight,
                    )}
                  >
                    <Icon className={cn('size-8', step.textColor)} />
                    <span
                      className={cn(
                        'absolute -top-1.5 -right-1.5 flex size-6 items-center justify-center rounded-full text-[11px] font-bold text-white shadow-sm',
                        step.color,
                      )}
                    >
                      {step.number}
                    </span>
                  </div>
                </div>

                {/* Content */}
                <div className="space-y-2 text-center">
                  <h3 className="text-lg font-semibold">{step.title}</h3>
                  <p className="text-sm leading-relaxed text-muted-foreground">
                    {step.description}
                  </p>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </section>
  );
}
