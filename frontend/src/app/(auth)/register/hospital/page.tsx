import type { Metadata } from 'next';
import Link from 'next/link';

import { AuthShell } from '@/components/layouts/auth-shell';
import { RegisterHospitalForm } from '@/features/auth/components/register-hospital-form';

export const metadata: Metadata = {
  title: 'Register Hospital | Healthcare HMS',
  description: 'Register a new hospital tenant on Healthcare HMS',
};

export default function RegisterHospitalPage() {
  return (
    <AuthShell
      title="Register hospital"
      description="Create your hospital tenant to begin onboarding administrators and clinical staff."
      footer={
        <p>
          Already registered?{' '}
          <Link href="/login" className="text-primary font-medium hover:underline">
            Sign in
          </Link>
        </p>
      }
    >
      <RegisterHospitalForm />
    </AuthShell>
  );
}
