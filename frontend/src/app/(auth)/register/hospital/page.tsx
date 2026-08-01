import type { Metadata } from 'next';
import Link from 'next/link';

import { AuthShell } from '@/components/layouts/auth-shell';
import { SinglePageRegistrationForm } from '@/features/auth/components/single-page-registration-form';

export const metadata: Metadata = {
  title: 'Register Hospital | Healthcare HMS',
  description: 'Create your hospital tenant and administrator account',
};

type Props = {
  searchParams: Promise<{ plan?: string }>;
};

export default async function RegisterHospitalPage({ searchParams }: Props) {
  const { plan } = await searchParams;
  const validPlans = ['BASIC', 'STANDARD', 'PREMIUM', 'ENTERPRISE'];
  const defaultPlan =
    plan && validPlans.includes(plan.toUpperCase()) ? plan.toUpperCase() : undefined;

  return (
    <AuthShell
      title="Create your account"
      description="Choose a plan on the pricing page, then register in one step."
      footer={
        <p>
          Already registered?{' '}
          <Link href="/login" className="text-primary font-medium hover:underline">
            Sign in
          </Link>
        </p>
      }
    >
      <SinglePageRegistrationForm defaultPlan={defaultPlan} />
    </AuthShell>
  );
}
