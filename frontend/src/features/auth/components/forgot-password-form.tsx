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
import { useForgotPasswordMutation } from '@/features/auth/hooks/use-forgot-password-mutation';
import {
  forgotPasswordSchema,
  type ForgotPasswordFormValues,
} from '@/features/auth/validation/forgot-password-schema';
import { getErrorMessage } from '@/lib/api-error';

/**
 * Requests a password recovery email. Always shows a generic success message.
 */
export function ForgotPasswordForm() {
  const [submitted, setSubmitted] = useState(false);
  const forgotPasswordMutation = useForgotPasswordMutation();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ForgotPasswordFormValues>({
    resolver: zodResolver(forgotPasswordSchema),
    defaultValues: {
      email: '',
    },
  });

  const onSubmit = handleSubmit(async (values) => {
    try {
      await forgotPasswordMutation.mutateAsync(values);
      setSubmitted(true);
      toast.success('Check your email for next steps');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to submit password reset request'));
    }
  });

  const isSubmitting = forgotPasswordMutation.isPending;
  const errorMessage = forgotPasswordMutation.isError
    ? getErrorMessage(forgotPasswordMutation.error)
    : null;

  if (submitted) {
    return (
      <AuthFormMessage
        variant="success"
        title="Check your email"
        description="If an account exists for that address, we sent a password reset link. The link expires in one hour and can be used once."
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
          'Send reset link'
        )}
      </Button>
    </form>
  );
}
