'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { CheckIcon, ChevronLeftIcon, ChevronRightIcon, Loader2Icon } from 'lucide-react';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { authApi } from '@/features/auth/api/auth-api';
import { AuthFormMessage } from '@/features/auth/components/auth-form-message';
import {
  RegistrationProgressBar,
  type StepNumber,
} from '@/features/auth/components/registration-progress-bar';
import type { HospitalRegistrationResponse } from '@/features/auth/types/auth.types';
import {
  adminAccountSchema,
  hospitalDetailsSchema,
  type AdminAccountValues,
  type HospitalDetailsValues,
} from '@/features/auth/validation/registration-steps';
import { getPlanById } from '@/features/pricing/data/plans';
import { getErrorMessage } from '@/lib/api-error';
import { cn } from '@/lib/utils';

type MultiStepRegistrationFormProps = {
  defaultPlan?: string;
};

type StepState = AdminAccountValues & HospitalDetailsValues & {
  subscriptionPlan: string;
  registrationToken?: string;
};

const initialStepState = (plan: string): StepState => ({
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
  registrationToken: undefined,
});

const PLAN_LABELS: Record<string, string> = {
  BASIC: 'Basic (Free)',
  STANDARD: 'Standard',
  PREMIUM: 'Premium',
  ENTERPRISE: 'Enterprise',
};

export function MultiStepRegistrationForm({ defaultPlan }: MultiStepRegistrationFormProps) {
  const selectedPlan = defaultPlan || 'BASIC';
  const [currentStep, setCurrentStep] = useState<StepNumber>(0);
  const [accumulated, setAccumulated] = useState<StepState>(initialStepState(selectedPlan));
  const [registration, setRegistration] = useState<HospitalRegistrationResponse | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [stepError, setStepError] = useState<string | null>(null);

  const advanceTo = (step: StepNumber) => {
    setStepError(null);
    setCurrentStep(step);
  };

  const planInfo = getPlanById(accumulated.subscriptionPlan);
  const planLabel = PLAN_LABELS[accumulated.subscriptionPlan] ?? accumulated.subscriptionPlan;

  // --- Step 0: Admin Account ---

  const accountForm = useForm<AdminAccountValues>({
    resolver: zodResolver(adminAccountSchema),
    defaultValues: { ...accumulated, adminPhone: accumulated.adminPhone || '' },
  });

  const onAccountSubmit = accountForm.handleSubmit(async (values) => {
    setStepError(null);
    setIsSubmitting(true);
    try {
      const result = await authApi.preRegisterAdmin({
        firstName: values.adminFirstName,
        lastName: values.adminLastName,
        email: values.adminEmail,
        password: values.adminPassword,
        phone: values.adminPhone?.trim() || undefined,
      });
      setAccumulated((prev) => ({
        ...prev,
        ...values,
        registrationToken: result.registrationToken,
        adminPhone: values.adminPhone || '',
      }));
      advanceTo(1);
      toast.success('Account details saved');
    } catch (error) {
      setStepError(getErrorMessage(error, 'Unable to validate account details'));
    } finally {
      setIsSubmitting(false);
    }
  });

  // --- Step 1: Hospital Setup ---

  const hospitalForm = useForm<HospitalDetailsValues>({
    resolver: zodResolver(hospitalDetailsSchema),
    defaultValues: {
      hospitalName: '',
      hospitalEmail: '',
      hospitalPhone: '',
      hospitalAddress: '',
    },
  });

  const onHospitalSubmit = hospitalForm.handleSubmit((values) => {
    setAccumulated((prev) => ({ ...prev, ...values }));
    advanceTo(2);
  });

  // --- Step 2: Confirm & Submit ---

  const onConfirm = async () => {
    setStepError(null);
    setIsSubmitting(true);
    try {
      const result = await authApi.completeRegistration({
        registrationToken: accumulated.registrationToken!,
        hospitalName: accumulated.hospitalName,
        hospitalEmail: accumulated.hospitalEmail,
        hospitalPhone: accumulated.hospitalPhone?.trim() || undefined,
        hospitalAddress: accumulated.hospitalAddress?.trim() || undefined,
        subscriptionPlan: accumulated.subscriptionPlan as 'BASIC' | 'STANDARD' | 'PREMIUM' | 'ENTERPRISE',
      });
      setRegistration(result);
      toast.success('Hospital registered successfully');
    } catch (error) {
      setStepError(getErrorMessage(error, 'Unable to complete registration'));
    } finally {
      setIsSubmitting(false);
    }
  };

  // --- Success screen ---

  if (registration) {
    return (
      <div className="space-y-6">
        <AuthFormMessage
          variant="success"
          title="Hospital registered"
          description={`Tenant "${registration.tenantSlug}" is ${registration.tenantStatus.toLowerCase()}. Admin ${registration.adminEmail} was created. Check email to verify the account, then sign in.`}
        />
        <div className="rounded-lg border p-4 text-sm text-muted-foreground">
          <h3 className="mb-2 font-medium text-foreground">What happens next?</h3>
          <ol className="ml-5 list-decimal space-y-1">
            <li>Check the admin email inbox for a verification link</li>
            <li>Verify your email address by clicking the link</li>
            <li>Sign in to start managing your hospital</li>
          </ol>
        </div>
        <a href="/login" className="inline-flex h-10 w-full items-center justify-center rounded-lg bg-primary px-4 text-sm font-medium text-primary-foreground hover:bg-primary/80">
          Sign in
        </a>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <RegistrationProgressBar currentStep={currentStep} />

      {/* Plan selection banner — shown when a plan was picked from /pricing */}
      {currentStep === 0 && selectedPlan !== 'BASIC' ? (
        <div className="rounded-lg border border-primary/20 bg-primary/5 p-3 text-sm">
          <span className="font-medium text-foreground">Selected plan: {planLabel}</span>
          {planInfo?.trialDays ? (
            <span className="ml-2 text-muted-foreground">
              — {planInfo.trialDays}-day free trial, no credit card required
            </span>
          ) : null}
        </div>
      ) : null}

      {stepError ? (
        <AuthFormMessage variant="error" title="Something went wrong" description={stepError} />
      ) : null}

      {/* Step 0: Admin Account */}
      {currentStep === 0 && (
        <form onSubmit={onAccountSubmit} className="space-y-5" noValidate>
          <div>
            <h2 className="text-lg font-semibold">Create your account</h2>
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
                aria-invalid={Boolean(accountForm.formState.errors.adminFirstName)}
                {...accountForm.register('adminFirstName')}
              />
              {accountForm.formState.errors.adminFirstName ? (
                <p className="text-destructive text-xs">
                  {accountForm.formState.errors.adminFirstName.message}
                </p>
              ) : null}
            </div>
            <div className="space-y-2">
              <Label htmlFor="adminLastName">Last name</Label>
              <Input
                id="adminLastName"
                autoComplete="family-name"
                aria-invalid={Boolean(accountForm.formState.errors.adminLastName)}
                {...accountForm.register('adminLastName')}
              />
              {accountForm.formState.errors.adminLastName ? (
                <p className="text-destructive text-xs">
                  {accountForm.formState.errors.adminLastName.message}
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
              aria-invalid={Boolean(accountForm.formState.errors.adminEmail)}
              {...accountForm.register('adminEmail')}
            />
            {accountForm.formState.errors.adminEmail ? (
              <p className="text-destructive text-xs">
                {accountForm.formState.errors.adminEmail.message}
              </p>
            ) : null}
          </div>

          <div className="space-y-2">
            <Label htmlFor="adminPassword">Password</Label>
            <Input
              id="adminPassword"
              type="password"
              autoComplete="new-password"
              aria-invalid={Boolean(accountForm.formState.errors.adminPassword)}
              {...accountForm.register('adminPassword')}
            />
            {accountForm.formState.errors.adminPassword ? (
              <p className="text-destructive text-xs">
                {accountForm.formState.errors.adminPassword.message}
              </p>
            ) : null}
          </div>

          <div className="space-y-2">
            <Label htmlFor="adminPhone">Phone (optional)</Label>
            <Input
              id="adminPhone"
              type="tel"
              autoComplete="tel"
              aria-invalid={Boolean(accountForm.formState.errors.adminPhone)}
              {...accountForm.register('adminPhone')}
            />
            {accountForm.formState.errors.adminPhone ? (
              <p className="text-destructive text-xs">
                {accountForm.formState.errors.adminPhone.message}
              </p>
            ) : null}
          </div>

          <Button type="submit" className="h-10 w-full" disabled={isSubmitting}>
            {isSubmitting ? (
              <>
                <Loader2Icon className="animate-spin" />
                Validating…
              </>
            ) : (
              <>
                Continue
                <ChevronRightIcon />
              </>
            )}
          </Button>
        </form>
      )}

      {/* Step 1: Hospital Setup */}
      {currentStep === 1 && (
        <form onSubmit={onHospitalSubmit} className="space-y-5" noValidate>
          <div>
            <h2 className="text-lg font-semibold">Set up your hospital</h2>
            <p className="text-sm text-muted-foreground">
              Tell us about your healthcare organization.
            </p>
          </div>

          <div className="rounded-lg border border-primary/20 bg-primary/5 p-3 text-sm">
            <span className="font-medium text-foreground">Plan: {planLabel}</span>
            {planInfo?.trialDays ? (
              <span className="ml-2 text-muted-foreground">
                — {planInfo.trialDays}-day free trial, no credit card required
              </span>
            ) : (
              <span className="ml-2 text-muted-foreground">
                — <a href="/pricing" className="underline">Change plan</a>
              </span>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="hospitalName">Hospital name</Label>
            <Input
              id="hospitalName"
              autoComplete="organization"
              placeholder="City Care Hospital"
              aria-invalid={Boolean(hospitalForm.formState.errors.hospitalName)}
              {...hospitalForm.register('hospitalName')}
            />
            {hospitalForm.formState.errors.hospitalName ? (
              <p className="text-destructive text-xs">
                {hospitalForm.formState.errors.hospitalName.message}
              </p>
            ) : null}
          </div>

          <div className="space-y-2">
            <Label htmlFor="hospitalEmail">Hospital email</Label>
            <Input
              id="hospitalEmail"
              type="email"
              autoComplete="email"
              placeholder="contact@citycare.com"
              aria-invalid={Boolean(hospitalForm.formState.errors.hospitalEmail)}
              {...hospitalForm.register('hospitalEmail')}
            />
            {hospitalForm.formState.errors.hospitalEmail ? (
              <p className="text-destructive text-xs">
                {hospitalForm.formState.errors.hospitalEmail.message}
              </p>
            ) : null}
          </div>

          <div className="space-y-2">
            <Label htmlFor="hospitalPhone">Phone (optional)</Label>
            <Input
              id="hospitalPhone"
              type="tel"
              autoComplete="tel"
              aria-invalid={Boolean(hospitalForm.formState.errors.hospitalPhone)}
              {...hospitalForm.register('hospitalPhone')}
            />
            {hospitalForm.formState.errors.hospitalPhone ? (
              <p className="text-destructive text-xs">
                {hospitalForm.formState.errors.hospitalPhone.message}
              </p>
            ) : null}
          </div>

          <div className="space-y-2">
            <Label htmlFor="hospitalAddress">Address (optional)</Label>
            <Input
              id="hospitalAddress"
              autoComplete="street-address"
              placeholder="120 Medical Center Drive"
              aria-invalid={Boolean(hospitalForm.formState.errors.hospitalAddress)}
              {...hospitalForm.register('hospitalAddress')}
            />
            {hospitalForm.formState.errors.hospitalAddress ? (
              <p className="text-destructive text-xs">
                {hospitalForm.formState.errors.hospitalAddress.message}
              </p>
            ) : null}
          </div>

          {/* Plan is no longer a dropdown here — it's selected on the /pricing page */}

          <div className="flex gap-3">
            <Button type="button" variant="outline" className="h-10 flex-1" onClick={() => advanceTo(0)}>
              <ChevronLeftIcon />
              Back
            </Button>
            <Button type="submit" className="h-10 flex-1" disabled={isSubmitting}>
              Continue
              <ChevronRightIcon />
            </Button>
          </div>
        </form>
      )}

      {/* Step 2: Review & Confirm */}
      {currentStep === 2 && (
        <div className="space-y-5">
          <div>
            <h2 className="text-lg font-semibold">Review and confirm</h2>
            <p className="text-sm text-muted-foreground">
              Please verify your details before creating the account.
            </p>
          </div>

          {/* Plan summary card */}
          <div className={cn(
            'rounded-lg border p-4',
            planInfo?.popular ? 'border-primary/30 bg-primary/5' : '',
          )}>
            <div className="flex items-start justify-between">
              <div>
                <h3 className="text-sm font-semibold text-muted-foreground">Plan</h3>
                <p className="mt-1 text-lg font-semibold">{planLabel}</p>
              </div>
              <a
                href="/pricing"
                className="text-primary text-xs font-medium underline-offset-2 hover:underline"
              >
                Change
              </a>
            </div>
            {planInfo?.trialDays ? (
              <p className="mt-1 text-xs text-muted-foreground">
                {planInfo.trialDays}-day free trial. No credit card required.
              </p>
            ) : null}
          </div>

          <div className="rounded-lg border p-4">
            <h3 className="mb-2 text-sm font-semibold text-muted-foreground">Administrator</h3>
            <dl className="space-y-1.5 text-sm">
              <div className="flex justify-between">
                <dt className="text-muted-foreground">Name</dt>
                <dd>
                  {accumulated.adminFirstName} {accumulated.adminLastName}
                </dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-muted-foreground">Email</dt>
                <dd>{accumulated.adminEmail}</dd>
              </div>
              {accumulated.adminPhone ? (
                <div className="flex justify-between">
                  <dt className="text-muted-foreground">Phone</dt>
                  <dd>{accumulated.adminPhone}</dd>
                </div>
              ) : null}
            </dl>
          </div>

          <div className="rounded-lg border p-4">
            <h3 className="mb-2 text-sm font-semibold text-muted-foreground">Hospital</h3>
            <dl className="space-y-1.5 text-sm">
              <div className="flex justify-between">
                <dt className="text-muted-foreground">Name</dt>
                <dd>{accumulated.hospitalName}</dd>
              </div>
              <div className="flex justify-between">
                <dt className="text-muted-foreground">Email</dt>
                <dd>{accumulated.hospitalEmail}</dd>
              </div>
              {accumulated.hospitalPhone ? (
                <div className="flex justify-between">
                  <dt className="text-muted-foreground">Phone</dt>
                  <dd>{accumulated.hospitalPhone}</dd>
                </div>
              ) : null}
              {accumulated.hospitalAddress ? (
                <div className="flex justify-between">
                  <dt className="text-muted-foreground">Address</dt>
                  <dd>{accumulated.hospitalAddress}</dd>
                </div>
              ) : null}
            </dl>
          </div>

          <div className="flex gap-3">
            <Button
              type="button"
              variant="outline"
              className="h-10 flex-1"
              onClick={() => advanceTo(1)}
            >
              <ChevronLeftIcon />
              Back
            </Button>
            <Button
              type="button"
              className="h-10 flex-[2]"
              disabled={isSubmitting}
              onClick={onConfirm}
            >
              {isSubmitting ? (
                <>
                  <Loader2Icon className="animate-spin" />
                  Registering…
                </>
              ) : (
                <>
                  <CheckIcon />
                  Create account
                </>
              )}
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
