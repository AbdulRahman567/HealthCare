import type { Metadata } from 'next';
import Link from 'next/link';

import { AuthShell } from '@/components/layouts/auth-shell';
import { MultiStepRegistrationForm } from '@/features/auth/components/multi-step-registration-form';

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
  const defaultPlan = plan && validPlans.includes(plan.toUpperCase()) ? plan.toUpperCase() : undefined;

  return (
    <AuthShell
      title="Create your account"
      description="Set up your hospital in just a few steps."
      footer={
        <p>
          Already registered?{' '}
          <Link href="/login" className="text-primary font-medium hover:underline">
            Sign in
          </Link>
        </p>
      }
    >
      <MultiStepRegistrationForm defaultPlan={defaultPlan} />
    </AuthShell>
  );
}
