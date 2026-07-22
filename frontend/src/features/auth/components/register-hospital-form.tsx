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
import { getErrorMessage } from '@/lib/api-error';

const emptyValues: RegisterHospitalFormValues = {
  hospitalName: '',
  hospitalEmail: '',
  hospitalPhone: '',
  hospitalAddress: '',
  subscriptionPlan: 'BASIC',
  adminFirstName: '',
  adminLastName: '',
  adminEmail: '',
  adminPassword: '',
  adminPhone: '',
};

export function RegisterHospitalForm() {
  const [registration, setRegistration] = useState<HospitalRegistrationResponse | null>(null);
  const registerMutation = useRegisterHospitalMutation();

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<RegisterHospitalFormValues>({
    resolver: zodResolver(registerHospitalSchema),
    defaultValues: emptyValues,
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
      reset(emptyValues);
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

        <div className="space-y-2">
          <Label htmlFor="subscriptionPlan">Subscription plan</Label>
          <select
            id="subscriptionPlan"
            className="border-input bg-background h-8 w-full rounded-lg border px-2.5 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 disabled:opacity-50"
            {...register('subscriptionPlan')}
          >
            <option value="BASIC">Basic</option>
            <option value="STANDARD">Standard</option>
            <option value="PREMIUM">Premium</option>
            <option value="ENTERPRISE">Enterprise</option>
          </select>
          {errors.subscriptionPlan ? (
            <p className="text-destructive text-xs">{errors.subscriptionPlan.message}</p>
          ) : null}
        </div>
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
