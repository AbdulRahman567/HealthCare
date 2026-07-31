'use client';

import {
  Activity,
  ArrowRight,
  Calendar,
  CheckCircle2,
  HeartPulse,
  Stethoscope,
  Users,
} from 'lucide-react';
import Link from 'next/link';

import { buttonVariants } from '@/components/ui/button';
import { cn } from '@/lib/utils';

const metrics = [
  { icon: Users, value: '1,284', label: 'Active patients', color: 'bg-emerald-500' },
  { icon: Calendar, value: '48', label: "Today's appointments", color: 'bg-blue-500' },
  { icon: Activity, value: '92%', label: 'Bed occupancy', color: 'bg-amber-500' },
  { icon: Stethoscope, value: '14', label: 'Doctors on duty', color: 'bg-violet-500' },
];

const activities = [
  {
    initials: 'JD',
    name: 'Jane Doe',
    department: 'Cardiology',
    status: 'Check-up',
    statusColor: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400',
    avatarColor: 'bg-gradient-to-br from-emerald-400 to-emerald-600',
  },
  {
    initials: 'MR',
    name: 'Mike Ross',
    department: 'Orthopedics',
    status: 'Surgery',
    statusColor: 'bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400',
    avatarColor: 'bg-gradient-to-br from-blue-400 to-blue-600',
  },
  {
    initials: 'AL',
    name: 'Anna Lee',
    department: 'Pediatrics',
    status: 'Follow-up',
    statusColor: 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400',
    avatarColor: 'bg-gradient-to-br from-amber-400 to-amber-600',
  },
];

export function HeroSection() {
  return (
    <section className="grid items-center gap-12 lg:grid-cols-2 lg:gap-20">
      {/* ---- Left: text ---- */}
      <div className="space-y-6">
        {/* Pill badge */}
        <div className="inline-flex items-center gap-1.5 rounded-full border border-primary/20 bg-primary/5 px-3.5 py-1 text-xs font-medium text-primary">
          <span className="relative flex size-2">
            <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-primary/40" />
            <span className="relative inline-flex size-2 rounded-full bg-primary" />
          </span>
          Now in public beta
        </div>

        {/* Headline */}
        <h1 className="text-4xl font-semibold tracking-tight text-balance md:text-5xl lg:text-6xl">
          <span className="bg-linear-to-r from-primary via-primary/80 to-cyan-600 bg-clip-text text-transparent">
            The operating system
          </span>
          <br />
          for modern hospitals
        </h1>
        <p className="text-muted-foreground max-w-xl text-base text-pretty md:text-lg">
          Secure multi-tenant platform for managing patients, appointments, staff, and clinical
          workflows — built for hospitals that outgrow spreadsheets.
        </p>

        {/* CTAs */}
        <div className="flex flex-col gap-3 sm:flex-row">
          <Link
            href="/register/hospital?plan=STANDARD"
            className={cn(
              buttonVariants({ size: 'lg' }),
              'group h-12 min-w-44 gap-2 rounded-xl px-6 text-base font-semibold shadow-sm transition-all duration-200 hover:shadow-md',
            )}
          >
            Start free trial
            <ArrowRight className="size-4 transition-transform duration-200 group-hover:translate-x-0.5" />
          </Link>
          <Link
            href="/pricing"
            className={cn(
              buttonVariants({ variant: 'outline', size: 'lg' }),
              'h-12 min-w-44 rounded-xl px-6 text-base font-medium',
            )}
          >
            View pricing
          </Link>
        </div>

        {/* Trusted-by mini-bar */}
        <div className="flex items-center gap-2 pt-2 text-xs text-muted-foreground">
          <div className="flex -space-x-1.5">
            {['/avatar-1.jpg', '/avatar-2.jpg', '/avatar-3.jpg'].map((_, i) => (
              <div
                key={i}
                className={cn(
                  'flex size-6 items-center justify-center rounded-full text-[10px] font-semibold text-white ring-2 ring-background',
                  'bg-gradient-to-br',
                  i === 0 && 'from-emerald-500 to-emerald-700',
                  i === 1 && 'from-blue-500 to-blue-700',
                  i === 2 && 'from-amber-500 to-amber-700',
                )}
              >
                {['JD', 'MR', 'AL'][i]}
              </div>
            ))}
          </div>
          <span>
            <strong className="text-foreground">500+</strong> hospitals already onboarded
          </span>
        </div>
      </div>

      {/* ---- Right: dashboard preview ---- */}
      <div className="relative hidden lg:block">
        {/* Glow behind the card */}
        <div className="pointer-events-none absolute -inset-8 rounded-[32px] bg-gradient-to-t from-primary/[0.07] to-transparent blur-xl" />

        {/* Main preview card */}
        <div className="relative rounded-2xl border border-black/5 bg-white/80 p-5 shadow-lg shadow-primary/5 ring-1 ring-black/[0.02] backdrop-blur-sm transition-shadow duration-300 hover:shadow-xl hover:shadow-primary/10 dark:bg-white/90">
          {/* Header row */}
          <div className="mb-5 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="flex size-8 items-center justify-center rounded-lg bg-primary text-white">
                <HeartPulse className="size-4" />
              </span>
              <span className="text-sm font-semibold">Healthcare HMS</span>
            </div>
            <div className="flex items-center gap-1.5 rounded-lg bg-emerald-50 px-2.5 py-1 text-xs font-medium text-emerald-700 dark:bg-emerald-900/20 dark:text-emerald-400">
              <span className="relative flex size-1.5">
                <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-emerald-500" />
                <span className="relative inline-flex size-1.5 rounded-full bg-emerald-500" />
              </span>
              Live
            </div>
          </div>

          {/* Metric badges row */}
          <div className="grid grid-cols-4 gap-2.5">
            {metrics.map((m) => {
              const Icon = m.icon;
              return (
                <div
                  key={m.label}
                  className="space-y-1 rounded-lg bg-primary/[0.03] p-2.5 transition-colors hover:bg-primary/[0.06]"
                >
                  <Icon className="text-primary size-4" />
                  <p className="text-sm font-bold tracking-tight">{m.value}</p>
                  <p className="text-[10px] text-muted-foreground">{m.label}</p>
                </div>
              );
            })}
          </div>

          {/* Section title */}
          <div className="mt-5 mb-2 flex items-center justify-between">
            <span className="text-xs font-semibold text-muted-foreground">RECENT ACTIVITY</span>
            <span className="text-[10px] text-muted-foreground">Today</span>
          </div>

          {/* Activity rows */}
          <div className="space-y-1">
            {activities.map((row, i) => (
              <div
                key={row.initials}
                className={cn(
                  'flex items-center gap-3 rounded-lg p-2 text-sm transition-colors hover:bg-primary/[0.03]',
                  i < activities.length - 1 && 'border-b border-black/[0.03]',
                )}
              >
                {/* Avatar */}
                <span
                  className={cn(
                    'flex size-7 shrink-0 items-center justify-center rounded-full text-[10px] font-bold text-white',
                    row.avatarColor,
                  )}
                >
                  {row.initials}
                </span>

                {/* Name + dept */}
                <div className="flex-1 truncate">
                  <span className="text-sm font-medium">{row.name}</span>
                  <span className="ml-2 text-xs text-muted-foreground">{row.department}</span>
                </div>

                {/* Status badge */}
                <span
                  className={cn(
                    'shrink-0 rounded-md px-2 py-0.5 text-[10px] font-semibold',
                    row.statusColor,
                  )}
                >
                  {row.status}
                </span>

                {/* Check icon */}
                <CheckCircle2 className="size-3.5 shrink-0 text-emerald-500" />
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}
