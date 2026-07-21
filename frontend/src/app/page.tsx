import { Activity, HeartPulse, ShieldCheck } from 'lucide-react';

import { Button } from '@/components/ui/button';

export default function Home() {
  return (
    <main className="mx-auto flex min-h-screen w-full max-w-5xl flex-col items-center justify-center gap-10 px-6 py-16">
      <section className="space-y-4 text-center">
        <h1 className="text-4xl font-semibold tracking-tight md:text-5xl">
          Healthcare Management System
        </h1>
        <p className="text-muted-foreground max-w-3xl text-balance text-base md:text-lg">
          Enterprise-grade, multi-tenant HMS foundation is ready. This phase delivers platform
          infrastructure, engineering quality gates, and production-ready runtime configuration.
        </p>
      </section>
      <section className="grid w-full gap-4 md:grid-cols-3">
        <article className="bg-card rounded-lg border p-5">
          <ShieldCheck className="mb-3 size-5" />
          <h2 className="text-sm font-medium">Security Baseline</h2>
          <p className="text-muted-foreground mt-2 text-sm">JWT-ready architecture and secure defaults.</p>
        </article>
        <article className="bg-card rounded-lg border p-5">
          <Activity className="mb-3 size-5" />
          <h2 className="text-sm font-medium">Observability Ready</h2>
          <p className="text-muted-foreground mt-2 text-sm">Actuator, Prometheus, and Grafana integration points.</p>
        </article>
        <article className="bg-card rounded-lg border p-5">
          <HeartPulse className="mb-3 size-5" />
          <h2 className="text-sm font-medium">Healthcare Focused</h2>
          <p className="text-muted-foreground mt-2 text-sm">Built for patient-safe, auditable clinical workflows.</p>
        </article>
      </section>
      <div className="flex items-center gap-3">
        <Button asChild>
          <a href="/api/health">Platform Health</a>
        </Button>
        <Button variant="outline" asChild>
          <a href="https://github.com">Repository</a>
        </Button>
      </div>
    </main>
  );
}
