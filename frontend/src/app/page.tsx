import type { Metadata } from 'next';

import { SiteHeader } from '@/components/layouts/site-header';
import { SiteFooter } from '@/components/layouts/site-footer';
import { CtaSection } from '@/features/marketing/components/cta-section';
import { FeaturesSection } from '@/features/marketing/components/features-section';
import { HeroSection } from '@/features/marketing/components/hero-section';
import { HowItWorksSection } from '@/features/marketing/components/how-it-works-section';
import { PricingTeaserSection } from '@/features/marketing/components/pricing-teaser-section';
import { TrustSection } from '@/features/marketing/components/trust-section';

export const metadata: Metadata = {
  title: 'Healthcare HMS — The Operating System for Modern Hospitals',
  description:
    'Enterprise multi-tenant Healthcare Management System — manage patients, appointments, staff, and clinical workflows. Start free.',
  openGraph: {
    title: 'Healthcare HMS',
    description:
      'Secure multi-tenant platform for managing patients, appointments, staff, and clinical workflows.',
  },
};

export default function LandingPage() {
  return (
    <div className="relative min-h-screen bg-[linear-gradient(180deg,#f4f8fb_0%,#ffffff_25%,#eef6fa_55%,#f4f8fb_100%)]">
      {/* Subtle grid overlay */}
      <div
        className="pointer-events-none absolute inset-0 bg-[image:radial-gradient(oklch(0.42_0.09_230/0.04)_1px,transparent_1px)] bg-[length:24px_24px] [mask-image:radial-gradient(ellipse_80%_50%_at_50%_0%,black,transparent)]"
        aria-hidden
      />

      <SiteHeader />

      <main className="relative mx-auto w-full max-w-6xl px-6 pb-28 pt-8 md:pt-14">
        {/* Sections with consistent vertical rhythm */}
        <section className="space-y-28 md:space-y-36">
          <HeroSection />

          <div className="border-t border-black/[0.04]" />

          <TrustSection />

          <div className="border-t border-black/[0.04]" />

          <FeaturesSection />

          <div className="border-t border-black/[0.04]" />

          <HowItWorksSection />

          <div className="border-t border-black/[0.04]" />

          <PricingTeaserSection />

          <CtaSection />
        </section>
      </main>

      <SiteFooter />
    </div>
  );
}
