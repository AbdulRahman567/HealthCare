'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { CheckIcon, Loader2Icon } from 'lucide-react';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import { z } from 'zod';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { authApi } from '@/features/auth/api/auth-api';
import { AuthFormMessage } from '@/features/auth/components/auth-form-message';
import type { SubscriptionPlan } from '@/features/auth/types/auth.types';
import {
  adminAccountSchema,
  hospitalDetailsSchema,
} from '@/features/auth/validation/registration-steps';
import { getPlanById } from '@/features/pricing/data/plans';
import { getErrorMessage } from '@/lib/api-error';

/**
 * Single-page registration (Phase 7). The full signup is collected on ONE scrollable page,
 * split into two visually distinct sections — "Your Account" and "Hospital Details" — so the
 * Phase 2 separation is preserved without a multi-step wizard.
 *
 * Submitting only creates a lightweight pending registration and sends a verification email.
 * No tenant/hospital/admin account exists until the emailed link is clicked.
 */
const registrationSchema = adminAccountSchema.merge(hospitalDetailsSchema).extend({
  subscriptionPlan: z.enum(['BASIC', 'STANDARD', 'PREMIUM', 'ENTERPRISE']),
});

type RegistrationFormValues = z.infer<typeof registrationSchema>;

const PLAN_LABELS: Record<string, string> = {
  BASIC: 'Basic (Free)',
  STANDARD: 'Standard',
  PREMIUM: 'Premium',
  ENTERPRISE: 'Enterprise',
};

type SinglePageRegistrationFormProps = {
  defaultPlan?: string;
};

export function SinglePageRegistrationForm({ defaultPlan }: SinglePageRegistrationFormProps) {
  const plan = (defaultPlan || 'BASIC') as SubscriptionPlan;
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submittedEmail, setSubmittedEmail] = useState<string | null>(null);
  const [formError, setFormError] = useState<string | null>(null);

  const planInfo = getPlanById(plan);
  const planLabel = PLAN_LABELS[plan] ?? plan;

  const form = useForm<RegistrationFormValues>({
    resolver: zodResolver(registrationSchema),
    defaultValues: {
      adminFirstName: '',
      adminLastName: '',
      adminEmail: '',
      adminPassword: '',
      adminPhone: '',
      hospitalName: '',
      hospitalEmail: '',
      hospitalPhone: '',
      hospitalAddress: '',
      subscriptionPlan: plan,
    },
  });

  const onSubmit = form.handleSubmit(async (values) => {
    setFormError(null);
    setIsSubmitting(true);
    try {
      const result = await authApi.submitRegistration({
        firstName: values.adminFirstName,
        lastName: values.adminLastName,
        email: values.adminEmail,
        password: values.adminPassword,
        phone: values.adminPhone?.trim() || undefined,
        hospitalName: values.hospitalName,
        hospitalEmail: values.hospitalEmail,
        hospitalPhone: values.hospitalPhone?.trim() || undefined,
        hospitalAddress: values.hospitalAddress?.trim() || undefined,
        subscriptionPlan: values.subscriptionPlan as SubscriptionPlan,
      });
      setSubmittedEmail(result.email);
      toast.success('Verification email sent — please check your inbox');
    } catch (error) {
      setFormError(getErrorMessage(error, 'Unable to submit registration'));
    } finally {
      setIsSubmitting(false);
    }
  });

  // Success screen — account NOT created yet, awaiting email verification.
  if (submittedEmail) {
    return (
      <div className="space-y-6">
        <AuthFormMessage
          variant="success"
          title="Check your inbox"
          description={`A verification email was sent to ${submittedEmail}. Your hospital account will be created once you verify your email — no account exists yet.`}
        />
        <div className="rounded-lg border p-4 text-sm text-muted-foreground">
          <h3 className="mb-2 font-medium text-foreground">What happens next?</h3>
          <ol className="ml-5 list-decimal space-y-1">
            <li>Open the verification link in the email we just sent</li>
            <li>Your hospital and administrator account are created instantly</li>
            <li>Sign in to start managing your hospital</li>
          </ol>
        </div>
        <a
          href="/register/hospital"
          className="bg-primary text-primary-foreground hover:bg-primary/80 inline-flex h-10 w-full items-center justify-center rounded-lg text-sm font-medium"
        >
          Resend verification email
        </a>
      </div>
    );
  }

  return (
    <form onSubmit={onSubmit} className="space-y-8" noValidate>
      {/* Plan summary */}
      <div className="rounded-lg border border-primary/20 bg-primary/5 p-3 text-sm">
        <span className="font-medium text-foreground">Selected plan: {planLabel}</span>
        {planInfo?.trialDays ? (
          <span className="ml-2 text-muted-foreground">
            — {planInfo.trialDays}-day free trial, no credit card required
          </span>
        ) : null}
        <span className="ml-2">
          <a href="/pricing" className="text-primary text-xs underline underline-offset-2">
            Change plan
          </a>
        </span>
      </div>

      {formError ? (
        <AuthFormMessage variant="error" title="Something went wrong" description={formError} />
      ) : null}

      {/* Section 1: Your Account */}
      <section aria-labelledby="account-section" className="space-y-5">
        <div>
          <h2 id="account-section" className="text-lg font-semibold">
            Your Account
          </h2>
          <p className="text-sm text-muted-foreground">
            This will be the hospital administrator account.
          </p>
        </div>

        <div className="grid gap-5 sm:grid-cols-2">
          <div className="space-y-2">
            <Label htmlFor="adminFirstName">First name</Label>
            <Input
              id="adminFirstName"
              autoComplete="given-name"
              aria-invalid={Boolean(form.formState.errors.adminFirstName)}
              {...form.register('adminFirstName')}
            />
            {form.formState.errors.adminFirstName ? (
              <p className="text-destructive text-xs">
                {form.formState.errors.adminFirstName.message}
              </p>
            ) : null}
          </div>
          <div className="space-y-2">
            <Label htmlFor="adminLastName">Last name</Label>
            <Input
              id="adminLastName"
              autoComplete="family-name"
              aria-invalid={Boolean(form.formState.errors.adminLastName)}
              {...form.register('adminLastName')}
            />
            {form.formState.errors.adminLastName ? (
              <p className="text-destructive text-xs">
                {form.formState.errors.adminLastName.message}
              </p>
            ) : null}
          </div>
        </div>

        <div className="space-y-2">
          <Label htmlFor="adminEmail">Email</Label>
          <Input
            id="adminEmail"
            type="email"
            autoComplete="email"
            placeholder="admin@hospital.com"
            aria-invalid={Boolean(form.formState.errors.adminEmail)}
            {...form.register('adminEmail')}
          />
          {form.formState.errors.adminEmail ? (
            <p className="text-destructive text-xs">{form.formState.errors.adminEmail.message}</p>
          ) : null}
        </div>

        <div className="space-y-2">
          <Label htmlFor="adminPassword">Password</Label>
          <Input
            id="adminPassword"
            type="password"
            autoComplete="new-password"
            aria-invalid={Boolean(form.formState.errors.adminPassword)}
            {...form.register('adminPassword')}
          />
          {form.formState.errors.adminPassword ? (
            <p className="text-destructive text-xs">
              {form.formState.errors.adminPassword.message}
            </p>
          ) : null}
        </div>

        <div className="space-y-2">
          <Label htmlFor="adminPhone">Phone (optional)</Label>
          <Input
            id="adminPhone"
            type="tel"
            autoComplete="tel"
            aria-invalid={Boolean(form.formState.errors.adminPhone)}
            {...form.register('adminPhone')}
          />
          {form.formState.errors.adminPhone ? (
            <p className="text-destructive text-xs">{form.formState.errors.adminPhone.message}</p>
          ) : null}
        </div>
      </section>

      {/* Section 2: Hospital Details */}
      <section aria-labelledby="hospital-section" className="space-y-5 border-t pt-8">
        <div>
          <h2 id="hospital-section" className="text-lg font-semibold">
            Hospital Details
          </h2>
          <p className="text-sm text-muted-foreground">
            Tell us about your healthcare organization.
          </p>
        </div>

        <div className="space-y-2">
          <Label htmlFor="hospitalName">Hospital name</Label>
          <Input
            id="hospitalName"
            autoComplete="organization"
            placeholder="City Care Hospital"
            aria-invalid={Boolean(form.formState.errors.hospitalName)}
            {...form.register('hospitalName')}
          />
          {form.formState.errors.hospitalName ? (
            <p className="text-destructive text-xs">{form.formState.errors.hospitalName.message}</p>
          ) : null}
        </div>

        <div className="space-y-2">
          <Label htmlFor="hospitalEmail">Hospital email</Label>
          <Input
            id="hospitalEmail"
            type="email"
            autoComplete="email"
            placeholder="contact@citycare.com"
            aria-invalid={Boolean(form.formState.errors.hospitalEmail)}
            {...form.register('hospitalEmail')}
          />
          {form.formState.errors.hospitalEmail ? (
            <p className="text-destructive text-xs">
              {form.formState.errors.hospitalEmail.message}
            </p>
          ) : null}
        </div>

        <div className="space-y-2">
          <Label htmlFor="hospitalPhone">Phone (optional)</Label>
          <Input
            id="hospitalPhone"
            type="tel"
            autoComplete="tel"
            aria-invalid={Boolean(form.formState.errors.hospitalPhone)}
            {...form.register('hospitalPhone')}
          />
          {form.formState.errors.hospitalPhone ? (
            <p className="text-destructive text-xs">
              {form.formState.errors.hospitalPhone.message}
            </p>
          ) : null}
        </div>

        <div className="space-y-2">
          <Label htmlFor="hospitalAddress">Address (optional)</Label>
          <Input
            id="hospitalAddress"
            autoComplete="street-address"
            placeholder="120 Medical Center Drive"
            aria-invalid={Boolean(form.formState.errors.hospitalAddress)}
            {...form.register('hospitalAddress')}
          />
          {form.formState.errors.hospitalAddress ? (
            <p className="text-destructive text-xs">
              {form.formState.errors.hospitalAddress.message}
            </p>
          ) : null}
        </div>
      </section>

      <Button type="submit" className="h-10 w-full" disabled={isSubmitting}>
        {isSubmitting ? (
          <>
            <Loader2Icon className="animate-spin" />
            Submitting…
          </>
        ) : (
          <>
            <CheckIcon />
            Create account
          </>
        )}
      </Button>

      <p className="text-center text-xs text-muted-foreground">
        By submitting you agree to the{' '}
        <a href="/pricing" className="underline underline-offset-2">
          terms
        </a>
        . Your hospital account is only created after you verify your email.
      </p>
    </form>
  );
}
