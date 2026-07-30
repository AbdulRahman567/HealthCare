'use client';

import { cn } from '@/lib/utils';

const STEP_LABELS = ['Account', 'Hospital', 'Confirm'];

export type StepNumber = 0 | 1 | 2;

export function RegistrationProgressBar({ currentStep }: { currentStep: StepNumber }) {
  return (
    <div className="mb-8" role="navigation" aria-label="Registration steps">
      <div className="flex items-center justify-between">
        {STEP_LABELS.map((label, index) => {
          const step = index as StepNumber;
          const isActive = step === currentStep;
          const isCompleted = step < currentStep;

          return (
            <div key={label} className="flex flex-1 items-center last:flex-none">
              <div className="flex flex-col items-center gap-1.5">
                <div
                  className={cn(
                    'flex size-8 items-center justify-center rounded-full text-sm font-semibold transition-colors',
                    isCompleted &&
                      'bg-primary text-primary-foreground',
                    isActive && !isCompleted &&
                      'border-primary text-primary border-2',
                    !isActive && !isCompleted &&
                      'border-muted-foreground/30 text-muted-foreground border-2',
                  )}
                  aria-current={isActive ? 'step' : undefined}
                >
                  {isCompleted ? (
                    <svg className="size-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={3}>
                      <path strokeLinecap="round" strokeLinejoin="round" d="M4.5 12.75l6 6 9-13.5" />
                    </svg>
                  ) : (
                    index + 1
                  )}
                </div>
                <span
                  className={cn(
                    'text-xs font-medium',
                    isActive && 'text-foreground',
                    isCompleted && 'text-primary',
                    !isActive && !isCompleted && 'text-muted-foreground',
                  )}
                >
                  {label}
                </span>
              </div>
              {index < STEP_LABELS.length - 1 && (
                <div
                  className={cn(
                    'mx-3 mt-[-1.5rem] h-0.5 flex-1 rounded transition-colors',
                    step <= currentStep ? 'bg-primary' : 'bg-muted-foreground/20',
                  )}
                />
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
