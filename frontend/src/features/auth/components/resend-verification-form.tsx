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
import { useResendVerificationMutation } from '@/features/auth/hooks/use-resend-verification-mutation';
import {
  resendVerificationSchema,
  type ResendVerificationFormValues,
} from '@/features/auth/validation/resend-verification-schema';
import { getErrorMessage } from '@/lib/api-error';

/**
 * Requests a new email verification link. Always shows a generic success message.
 */
export function ResendVerificationForm() {
  const [submitted, setSubmitted] = useState(false);
  const resendMutation = useResendVerificationMutation();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ResendVerificationFormValues>({
    resolver: zodResolver(resendVerificationSchema),
    defaultValues: {
      email: '',
    },
  });

  const onSubmit = handleSubmit(async (values) => {
    try {
      await resendMutation.mutateAsync(values);
      setSubmitted(true);
      toast.success('Check your email for next steps');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to resend verification email'));
    }
  });

  const isSubmitting = resendMutation.isPending;
  const errorMessage = resendMutation.isError ? getErrorMessage(resendMutation.error) : null;

  if (submitted) {
    return (
      <AuthFormMessage
        variant="success"
        title="Check your email"
        description="If an unverified account exists for that address, we sent a new verification link. The link expires in 24 hours and can be used once."
      />
    );
  }

  return (
    <form onSubmit={onSubmit} className="space-y-5" noValidate>
      {errorMessage ? (
        <AuthFormMessage variant="error" title="Request failed" description={errorMessage} />
      ) : null}

      <div className="space-y-2">
        <Label htmlFor="email">Work email</Label>
        <Input
          id="email"
          type="email"
          autoComplete="email"
          placeholder="you@hospital.com"
          aria-invalid={Boolean(errors.email)}
          disabled={isSubmitting}
          {...register('email')}
        />
        {errors.email ? <p className="text-destructive text-xs">{errors.email.message}</p> : null}
      </div>

      <Button type="submit" className="h-10 w-full" disabled={isSubmitting}>
        {isSubmitting ? (
          <>
            <Loader2Icon className="animate-spin" />
            Sending link…
          </>
        ) : (
          'Resend verification link'
        )}
      </Button>
    </form>
  );
}
