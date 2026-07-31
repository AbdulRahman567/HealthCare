import { Activity, Building2, Globe, ShieldCheck, Users } from 'lucide-react';

const stats = [
  {
    icon: Building2,
    value: '500+',
    label: 'Hospitals onboarded',
    description: 'Across 30+ countries',
  },
  {
    icon: Users,
    value: '50K+',
    label: 'Healthcare professionals',
    description: 'Using the platform daily',
  },
  {
    icon: Activity,
    value: '2M+',
    label: 'Patients managed',
    description: 'And growing every month',
  },
  {
    icon: ShieldCheck,
    value: '99.9%',
    label: 'Platform uptime',
    description: 'Enterprise-grade reliability',
  },
];

const logoCloud = [
  'City General Hospital',
  'Sunrise Medical Center',
  'Pediatric Partners',
  'Valley Health System',
  'Lakeside Medical Group',
  'Coastal Health Alliance',
];

export function TrustSection() {
  return (
    <section className="space-y-12">
      {/* Section label */}
      <div className="text-center">
        <span className="inline-flex items-center gap-1.5 rounded-full border border-primary/15 bg-primary/[0.04] px-3.5 py-1 text-xs font-medium text-primary">
          <Globe className="size-3" />
          Trusted worldwide
        </span>
      </div>

      {/* Stats grid */}
      <div className="grid grid-cols-2 gap-4 md:grid-cols-4 md:gap-6">
        {stats.map((stat) => {
          const Icon = stat.icon;
          return (
            <div
              key={stat.label}
              className="group relative space-y-3 rounded-xl border border-black/[0.04] bg-white/70 p-5 text-center shadow-xs transition-all duration-200 hover:border-primary/20 hover:shadow-sm hover:shadow-primary/[0.03]"
            >
              {/* Icon */}
              <div className="flex justify-center">
                <span className="flex size-10 items-center justify-center rounded-xl bg-primary/[0.07] text-primary transition-colors duration-200 group-hover:bg-primary/[0.12]">
                  <Icon className="size-5" />
                </span>
              </div>

              {/* Value */}
              <p className="text-2xl font-bold tracking-tight">{stat.value}</p>

              {/* Label */}
              <p className="text-sm font-medium text-foreground">{stat.label}</p>

              {/* Description */}
              <p className="text-xs text-muted-foreground">{stat.description}</p>
            </div>
          );
        })}
      </div>

      {/* Divider */}
      <div className="relative">
        <div className="absolute inset-0 flex items-center">
          <div className="w-full border-t border-black/[0.06]" />
        </div>
      </div>

      {/* Logo cloud */}
      <div className="space-y-5">
        <p className="text-center text-xs font-medium tracking-wide text-muted-foreground uppercase">
          Used by healthcare facilities nationwide
        </p>

        <div className="flex flex-wrap items-center justify-center gap-x-8 gap-y-4">
          {logoCloud.map((name) => (
            <span
              key={name}
              className="rounded-lg px-4 py-2 text-sm font-medium tracking-tight text-muted-foreground/70 transition-colors duration-200 hover:text-muted-foreground"
            >
              {name}
            </span>
          ))}
        </div>
      </div>
    </section>
  );
}
