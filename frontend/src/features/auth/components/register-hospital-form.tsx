'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2Icon } from 'lucide-react';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { AuthFormMessage } from '@/features/auth/components/auth-form-message';
import { useRegisterHospitalMutation } from '@/features/auth/hooks/use-register-hospital-mutation';
import type { HospitalRegistrationResponse } from '@/features/auth/types/auth.types';
import {
  registerHospitalSchema,
  type RegisterHospitalFormValues,
} from '@/features/auth/validation/register-hospital-schema';
import { getPlanById } from '@/features/pricing/data/plans';
import { getErrorMessage } from '@/lib/api-error';

type RegisterHospitalFormProps = {
  defaultPlan?: string;
};

const PLAN_LABELS: Record<string, string> = {
  BASIC: 'Basic (Free)',
  STANDARD: 'Standard',
  PREMIUM: 'Premium',
  ENTERPRISE: 'Enterprise',
};

export function RegisterHospitalForm({ defaultPlan }: RegisterHospitalFormProps) {
  const selectedPlan = defaultPlan || 'BASIC';
  const [registration, setRegistration] = useState<HospitalRegistrationResponse | null>(null);
  const registerMutation = useRegisterHospitalMutation();
  const planInfo = getPlanById(selectedPlan);
  const planLabel = PLAN_LABELS[selectedPlan] ?? selectedPlan;

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<RegisterHospitalFormValues>({
    resolver: zodResolver(registerHospitalSchema),
    defaultValues: {
      hospitalName: '',
      hospitalEmail: '',
      hospitalPhone: '',
      hospitalAddress: '',
      subscriptionPlan: selectedPlan as 'BASIC' | 'STANDARD' | 'PREMIUM' | 'ENTERPRISE',
      adminFirstName: '',
      adminLastName: '',
      adminEmail: '',
      adminPassword: '',
      adminPhone: '',
    },
  });

  const onSubmit = handleSubmit(async (values) => {
    setRegistration(null);
    try {
      const result = await registerMutation.mutateAsync({
        hospitalName: values.hospitalName,
        hospitalEmail: values.hospitalEmail,
        hospitalPhone: values.hospitalPhone?.trim() ? values.hospitalPhone.trim() : undefined,
        hospitalAddress: values.hospitalAddress?.trim() ? values.hospitalAddress.trim() : undefined,
        subscriptionPlan: values.subscriptionPlan,
        adminFirstName: values.adminFirstName,
        adminLastName: values.adminLastName,
        adminEmail: values.adminEmail,
        adminPassword: values.adminPassword,
        adminPhone: values.adminPhone?.trim() ? values.adminPhone.trim() : undefined,
      });
      setRegistration(result);
      reset({
        hospitalName: '',
        hospitalEmail: '',
        hospitalPhone: '',
        hospitalAddress: '',
        subscriptionPlan: selectedPlan as 'BASIC' | 'STANDARD' | 'PREMIUM' | 'ENTERPRISE',
        adminFirstName: '',
        adminLastName: '',
        adminEmail: '',
        adminPassword: '',
        adminPhone: '',
      });
      toast.success('Hospital registered successfully');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to register hospital'));
    }
  });

  const isSubmitting = registerMutation.isPending;
  const errorMessage = registerMutation.isError ? getErrorMessage(registerMutation.error) : null;

  return (
    <form onSubmit={onSubmit} className="space-y-5" noValidate>
      {errorMessage ? (
        <AuthFormMessage variant="error" title="Registration failed" description={errorMessage} />
      ) : null}

      {registration ? (
        <AuthFormMessage
          variant="success"
          title="Hospital registered"
          description={`Tenant ${registration.tenantSlug} is ${registration.tenantStatus}. Admin ${registration.adminEmail} was created. Check email to verify the account, then sign in.`}
        />
      ) : null}

      {/* Plan selection banner */}
      <div className="rounded-lg border border-primary/20 bg-primary/5 p-3 text-sm">
        <span className="font-medium text-foreground">Plan: {planLabel}</span>
        {planInfo?.trialDays ? (
          <span className="ml-2 text-muted-foreground">
            — {planInfo.trialDays}-day free trial, no credit card required
          </span>
        ) : null}
        <a href="/pricing" className="ml-2 text-primary underline underline-offset-2">
          Change plan
        </a>
      </div>

      <fieldset className="space-y-5" disabled={isSubmitting}>
        <legend className="text-sm font-medium">Hospital</legend>

        <div className="space-y-2">
          <Label htmlFor="hospitalName">Hospital name</Label>
          <Input
            id="hospitalName"
            autoComplete="organization"
            placeholder="City Care Hospital"
            aria-invalid={Boolean(errors.hospitalName)}
            {...register('hospitalName')}
          />
          {errors.hospitalName ? (
            <p className="text-destructive text-xs">{errors.hospitalName.message}</p>
          ) : null}
        </div>

        <div className="space-y-2">
          <Label htmlFor="hospitalEmail">Hospital email</Label>
          <Input
            id="hospitalEmail"
            type="email"
            autoComplete="email"
            placeholder="contact@citycare.com"
            aria-invalid={Boolean(errors.hospitalEmail)}
            {...register('hospitalEmail')}
          />
          {errors.hospitalEmail ? (
            <p className="text-destructive text-xs">{errors.hospitalEmail.message}</p>
          ) : null}
        </div>

        <div className="space-y-2">
          <Label htmlFor="hospitalPhone">Phone (optional)</Label>
          <Input
            id="hospitalPhone"
            type="tel"
            autoComplete="tel"
            placeholder="+1 555 0100"
            aria-invalid={Boolean(errors.hospitalPhone)}
            {...register('hospitalPhone')}
          />
          {errors.hospitalPhone ? (
            <p className="text-destructive text-xs">{errors.hospitalPhone.message}</p>
          ) : null}
        </div>

        <div className="space-y-2">
          <Label htmlFor="hospitalAddress">Address (optional)</Label>
          <Input
            id="hospitalAddress"
            autoComplete="street-address"
            placeholder="120 Medical Center Drive"
            aria-invalid={Boolean(errors.hospitalAddress)}
            {...register('hospitalAddress')}
          />
          {errors.hospitalAddress ? (
            <p className="text-destructive text-xs">{errors.hospitalAddress.message}</p>
          ) : null}
        </div>

        {/* Subscription plan is hidden — selected from /pricing page */}
        <input type="hidden" {...register('subscriptionPlan')} />
      </fieldset>

      <fieldset className="space-y-5" disabled={isSubmitting}>
        <legend className="text-sm font-medium">Initial administrator</legend>

        <div className="grid gap-5 sm:grid-cols-2">
          <div className="space-y-2">
            <Label htmlFor="adminFirstName">First name</Label>
            <Input
              id="adminFirstName"
              autoComplete="given-name"
              aria-invalid={Boolean(errors.adminFirstName)}
              {...register('adminFirstName')}
            />
            {errors.adminFirstName ? (
              <p className="text-destructive text-xs">{errors.adminFirstName.message}</p>
            ) : null}
          </div>
          <div className="space-y-2">
            <Label htmlFor="adminLastName">Last name</Label>
            <Input
              id="adminLastName"
              autoComplete="family-name"
              aria-invalid={Boolean(errors.adminLastName)}
              {...register('adminLastName')}
            />
            {errors.adminLastName ? (
              <p className="text-destructive text-xs">{errors.adminLastName.message}</p>
            ) : null}
          </div>
        </div>

        <div className="space-y-2">
          <Label htmlFor="adminEmail">Admin email</Label>
          <Input
            id="adminEmail"
            type="email"
            autoComplete="email"
            placeholder="admin@citycare.com"
            aria-invalid={Boolean(errors.adminEmail)}
            {...register('adminEmail')}
          />
          {errors.adminEmail ? (
            <p className="text-destructive text-xs">{errors.adminEmail.message}</p>
          ) : null}
        </div>

        <div className="space-y-2">
          <Label htmlFor="adminPassword">Admin password</Label>
          <Input
            id="adminPassword"
            type="password"
            autoComplete="new-password"
            aria-invalid={Boolean(errors.adminPassword)}
            {...register('adminPassword')}
          />
          {errors.adminPassword ? (
            <p className="text-destructive text-xs">{errors.adminPassword.message}</p>
          ) : null}
        </div>

        <div className="space-y-2">
          <Label htmlFor="adminPhone">Admin phone (optional)</Label>
          <Input
            id="adminPhone"
            type="tel"
            autoComplete="tel"
            aria-invalid={Boolean(errors.adminPhone)}
            {...register('adminPhone')}
          />
          {errors.adminPhone ? (
            <p className="text-destructive text-xs">{errors.adminPhone.message}</p>
          ) : null}
        </div>
      </fieldset>

      <Button type="submit" className="h-10 w-full" disabled={isSubmitting}>
        {isSubmitting ? (
          <>
            <Loader2Icon className="animate-spin" />
            Registering…
          </>
        ) : (
          'Register hospital'
        )}
      </Button>
    </form>
  );
}
