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
    defaultValues: {
      hospitalName: '',
      email: '',
      phone: '',
      address: '',
      subscriptionPlan: 'BASIC',
    },
  });

  const onSubmit = handleSubmit(async (values) => {
    setRegistration(null);
    try {
      const result = await registerMutation.mutateAsync({
        hospitalName: values.hospitalName,
        email: values.email,
        phone: values.phone?.trim() ? values.phone.trim() : undefined,
        address: values.address?.trim() ? values.address.trim() : undefined,
        subscriptionPlan: values.subscriptionPlan,
      });
      setRegistration(result);
      reset({
        hospitalName: '',
        email: '',
        phone: '',
        address: '',
        subscriptionPlan: 'BASIC',
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
          description={`Tenant created in PENDING status for ${registration.name}. Tenant ID: ${registration.tenantId}. Next, register the initial hospital admin (API: POST /api/v1/auth/register/admin). The hospital activates after the admin verifies their email.`}
        />
      ) : null}

      <div className="space-y-2">
        <Label htmlFor="hospitalName">Hospital name</Label>
        <Input
          id="hospitalName"
          autoComplete="organization"
          placeholder="City Care Hospital"
          aria-invalid={Boolean(errors.hospitalName)}
          disabled={isSubmitting}
          {...register('hospitalName')}
        />
        {errors.hospitalName ? (
          <p className="text-destructive text-xs">{errors.hospitalName.message}</p>
        ) : null}
      </div>

      <div className="space-y-2">
        <Label htmlFor="email">Hospital email</Label>
        <Input
          id="email"
          type="email"
          autoComplete="email"
          placeholder="contact@citycare.com"
          aria-invalid={Boolean(errors.email)}
          disabled={isSubmitting}
          {...register('email')}
        />
        {errors.email ? <p className="text-destructive text-xs">{errors.email.message}</p> : null}
      </div>

      <div className="space-y-2">
        <Label htmlFor="phone">Phone (optional)</Label>
        <Input
          id="phone"
          type="tel"
          autoComplete="tel"
          placeholder="+1 555 0100"
          aria-invalid={Boolean(errors.phone)}
          disabled={isSubmitting}
          {...register('phone')}
        />
        {errors.phone ? <p className="text-destructive text-xs">{errors.phone.message}</p> : null}
      </div>

      <div className="space-y-2">
        <Label htmlFor="address">Address (optional)</Label>
        <Input
          id="address"
          autoComplete="street-address"
          placeholder="120 Medical Center Drive"
          aria-invalid={Boolean(errors.address)}
          disabled={isSubmitting}
          {...register('address')}
        />
        {errors.address ? <p className="text-destructive text-xs">{errors.address.message}</p> : null}
      </div>

      <div className="space-y-2">
        <Label htmlFor="subscriptionPlan">Subscription plan</Label>
        <select
          id="subscriptionPlan"
          className="border-input bg-background h-8 w-full rounded-lg border px-2.5 text-sm outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50 disabled:opacity-50"
          disabled={isSubmitting}
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
