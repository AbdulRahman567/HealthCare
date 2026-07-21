'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2Icon } from 'lucide-react';
import Link from 'next/link';
import { useRouter, useSearchParams } from 'next/navigation';
import { useMemo } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { AuthFormMessage } from '@/features/auth/components/auth-form-message';
import { useResetPasswordMutation } from '@/features/auth/hooks/use-reset-password-mutation';
import {
  resetPasswordSchema,
  type ResetPasswordFormValues,
} from '@/features/auth/validation/reset-password-schema';
import { getErrorMessage } from '@/lib/api-error';

/**
 * Completes password recovery using the token from the email reset link.
 */
export function ResetPasswordForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const tokenFromQuery = useMemo(() => searchParams.get('token')?.trim() ?? '', [searchParams]);
  const resetPasswordMutation = useResetPasswordMutation();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ResetPasswordFormValues>({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: {
      newPassword: '',
      confirmPassword: '',
    },
  });

  const onSubmit = handleSubmit(async (values) => {
    try {
      await resetPasswordMutation.mutateAsync({
        token: tokenFromQuery,
        newPassword: values.newPassword,
      });
      toast.success('Password updated. You can sign in now.');
      router.replace('/login');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to reset password'));
    }
  });

  const isSubmitting = resetPasswordMutation.isPending;
  const errorMessage = resetPasswordMutation.isError
    ? getErrorMessage(resetPasswordMutation.error)
    : null;

  if (!tokenFromQuery || tokenFromQuery.length < 32) {
    return (
      <div className="space-y-5">
        <AuthFormMessage
          variant="error"
          title="Invalid reset link"
          description="This password reset link is missing or incomplete. Request a new link from the forgot password page."
        />
        <Link
          href="/forgot-password"
          className="bg-primary text-primary-foreground hover:bg-primary/80 inline-flex h-10 w-full items-center justify-center rounded-lg text-sm font-medium"
        >
          Request a new link
        </Link>
      </div>
    );
  }

  return (
    <form onSubmit={onSubmit} className="space-y-5" noValidate>
      {errorMessage ? (
        <AuthFormMessage variant="error" title="Reset failed" description={errorMessage} />
      ) : null}

      <div className="space-y-2">
        <Label htmlFor="newPassword">New password</Label>
        <Input
          id="newPassword"
          type="password"
          autoComplete="new-password"
          placeholder="At least 12 characters"
          aria-invalid={Boolean(errors.newPassword)}
          disabled={isSubmitting}
          {...register('newPassword')}
        />
        {errors.newPassword ? (
          <p className="text-destructive text-xs">{errors.newPassword.message}</p>
        ) : (
          <p className="text-muted-foreground text-xs">
            Use uppercase, lowercase, a number, and a special character.
          </p>
        )}
      </div>

      <div className="space-y-2">
        <Label htmlFor="confirmPassword">Confirm password</Label>
        <Input
          id="confirmPassword"
          type="password"
          autoComplete="new-password"
          placeholder="Re-enter your new password"
          aria-invalid={Boolean(errors.confirmPassword)}
          disabled={isSubmitting}
          {...register('confirmPassword')}
        />
        {errors.confirmPassword ? (
          <p className="text-destructive text-xs">{errors.confirmPassword.message}</p>
        ) : null}
      </div>

      <Button type="submit" className="h-10 w-full" disabled={isSubmitting}>
        {isSubmitting ? (
          <>
            <Loader2Icon className="animate-spin" />
            Updating password…
          </>
        ) : (
          'Reset password'
        )}
      </Button>
    </form>
  );
}
