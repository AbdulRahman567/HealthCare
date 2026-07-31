'use client';

import {
  Banknote,
  Calendar,
  ChartLine,
  ClipboardList,
  HeartPulse,
  MessagesSquare,
  ShieldCheck,
  Stethoscope,
} from 'lucide-react';
import { useState } from 'react';

import { cn } from '@/lib/utils';

const features = [
  {
    icon: Stethoscope,
    title: 'Patient Management',
    description:
      'Comprehensive patient records, medical history, and profiles at your fingertips — all in one secure, searchable system.',
    color: 'from-emerald-500 to-emerald-600',
    bgLight: 'bg-emerald-50 dark:bg-emerald-950/30',
    textColor: 'text-emerald-600 dark:text-emerald-400',
  },
  {
    icon: Calendar,
    title: 'Appointment Scheduling',
    description:
      'Smart calendar with queue management, availability scheduling, automated reminders, and easy rescheduling for patients.',
    color: 'from-blue-500 to-blue-600',
    bgLight: 'bg-blue-50 dark:bg-blue-950/30',
    textColor: 'text-blue-600 dark:text-blue-400',
  },
  {
    icon: ShieldCheck,
    title: 'Clinical Workflows',
    description:
      'SOAP notes, structured templates, prescription management, and clinical decision support built for modern care teams.',
    color: 'from-violet-500 to-violet-600',
    bgLight: 'bg-violet-50 dark:bg-violet-950/30',
    textColor: 'text-violet-600 dark:text-violet-400',
  },
  {
    icon: Banknote,
    title: 'Billing & Insurance',
    description:
      'Automated invoicing, insurance claims processing, payment tracking, and financial reporting for your entire practice.',
    color: 'from-amber-500 to-amber-600',
    bgLight: 'bg-amber-50 dark:bg-amber-950/30',
    textColor: 'text-amber-600 dark:text-amber-400',
  },
];

const extendedFeatures = [
  {
    icon: ClipboardList,
    title: 'Inventory & Pharmacy',
    description:
      'Track medications, supplies, and equipment with automated reorder alerts and expiry management.',
    color: 'from-cyan-500 to-cyan-600',
    bgLight: 'bg-cyan-50 dark:bg-cyan-950/30',
    textColor: 'text-cyan-600 dark:text-cyan-400',
  },
  {
    icon: MessagesSquare,
    title: 'Secure Messaging',
    description:
      'HIPAA-compliant internal messaging, referral coordination, and patient communication all in one place.',
    color: 'from-rose-500 to-rose-600',
    bgLight: 'bg-rose-50 dark:bg-rose-950/30',
    textColor: 'text-rose-600 dark:text-rose-400',
  },
  {
    icon: ChartLine,
    title: 'Analytics & Reports',
    description:
      'Real-time dashboards, operational metrics, and custom reports for data-driven hospital decisions.',
    color: 'from-indigo-500 to-indigo-600',
    bgLight: 'bg-indigo-50 dark:bg-indigo-950/30',
    textColor: 'text-indigo-600 dark:text-indigo-400',
  },
  {
    icon: HeartPulse,
    title: 'Emergency & Triage',
    description:
      'Fast-track emergency intake, severity-based triage, and real-time bed management for critical care.',
    color: 'from-orange-500 to-orange-600',
    bgLight: 'bg-orange-50 dark:bg-orange-950/30',
    textColor: 'text-orange-600 dark:text-orange-400',
  },
];

export function FeaturesSection() {
  const [showAll, setShowAll] = useState(false);
  const displayed = showAll ? [...features, ...extendedFeatures] : features;

  return (
    <section className="space-y-10">
      {/* Header */}
      <div className="mx-auto max-w-2xl text-center">
        <span className="inline-flex items-center gap-1.5 rounded-full border border-primary/15 bg-primary/[0.04] px-3.5 py-1 text-xs font-medium text-primary">
          Everything you need
        </span>
        <h2 className="mt-4 text-3xl font-semibold tracking-tight text-balance md:text-4xl">
          One platform, designed for healthcare
        </h2>
        <p className="text-muted-foreground mt-3 max-w-lg text-sm text-pretty md:mx-auto">
          From registration to billing, manage your entire hospital operations from a single, secure
          dashboard.
        </p>
      </div>

      {/* Feature cards */}
      <div className="grid gap-5 md:grid-cols-2">
        {displayed.map((feature) => {
          const Icon = feature.icon;
          return (
            <article
              key={feature.title}
              className="group relative overflow-hidden rounded-2xl border border-black/[0.04] bg-white/70 p-6 shadow-xs transition-all duration-300 hover:border-primary/15 hover:shadow-md hover:shadow-primary/[0.03]"
            >
              {/* Hover gradient accent */}
              <div className="pointer-events-none absolute -inset-px rounded-2xl opacity-0 transition-opacity duration-300 group-hover:opacity-100">
                <div className="absolute inset-0 rounded-2xl bg-gradient-to-br from-primary/[0.02] to-transparent" />
              </div>

              <div className="relative flex gap-4">
                {/* Icon */}
                <span
                  className={cn(
                    'flex size-11 shrink-0 items-center justify-center rounded-xl',
                    feature.bgLight,
                    feature.textColor,
                  )}
                >
                  <Icon className="size-5" />
                </span>

                <div className="space-y-2">
                  <h3 className="font-semibold">{feature.title}</h3>
                  <p className="text-sm leading-relaxed text-muted-foreground">
                    {feature.description}
                  </p>
                </div>
              </div>
            </article>
          );
        })}
      </div>

      {/* Toggle button */}
      <div className="text-center">
        <button
          type="button"
          onClick={() => setShowAll(!showAll)}
          className={cn(
            'inline-flex items-center gap-1.5 rounded-lg border px-4 py-2 text-sm font-medium transition-all duration-200',
            'border-primary/20 text-primary hover:bg-primary/5 active:bg-primary/10',
          )}
        >
          {showAll ? 'Show fewer features' : 'View all features'}
          <span className="text-xs text-muted-foreground">({extendedFeatures.length} more)</span>
        </button>
      </div>
    </section>
  );
}
