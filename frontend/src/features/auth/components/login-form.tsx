'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2Icon } from 'lucide-react';
import Link from 'next/link';
import { useRouter, useSearchParams } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { AuthFormMessage } from '@/features/auth/components/auth-form-message';
import { useLoginMutation } from '@/features/auth/hooks/use-login-mutation';
import { loginSchema, type LoginFormValues } from '@/features/auth/validation/login-schema';
import { getErrorMessage, ApiClientError } from '@/lib/api-error';
import { useSession } from '@/providers/session-provider';

export function LoginForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { signIn } = useSession();
  const loginMutation = useLoginMutation();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  });

  const onSubmit = handleSubmit(async (values) => {
    try {
      const result = await loginMutation.mutateAsync(values);
      signIn(result);
      toast.success('Signed in successfully');
      const nextPath = searchParams.get('next');
      router.replace(nextPath && nextPath.startsWith('/') ? nextPath : '/app');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to sign in'));
    }
  });

  const isSubmitting = loginMutation.isPending;
  const errorMessage = loginMutation.isError ? getErrorMessage(loginMutation.error) : null;
  const isEmailUnverified =
    loginMutation.isError &&
    loginMutation.error instanceof ApiClientError &&
    loginMutation.error.errorCode === 'EMAIL_NOT_VERIFIED';

  return (
    <form onSubmit={onSubmit} className="space-y-5" noValidate>
      {errorMessage ? (
        <AuthFormMessage variant="error" title="Sign in failed" description={errorMessage} />
      ) : null}

      {isEmailUnverified ? (
        <p className="text-sm">
          Need a new link?{' '}
          <Link href="/resend-verification" className="text-primary font-medium hover:underline">
            Resend verification email
          </Link>
        </p>
      ) : null}

      <div className="space-y-2">
        <Label htmlFor="email">Email</Label>
        <Input
          id="email"
          type="email"
          autoComplete="email"
          placeholder="admin@hospital.com"
          aria-invalid={Boolean(errors.email)}
          disabled={isSubmitting}
          {...register('email')}
        />
        {errors.email ? <p className="text-destructive text-xs">{errors.email.message}</p> : null}
      </div>

      <div className="space-y-2">
        <div className="flex items-center justify-between gap-3">
          <Label htmlFor="password">Password</Label>
          <Link
            href="/forgot-password"
            className="text-primary text-xs font-medium hover:underline"
          >
            Forgot password?
          </Link>
        </div>
        <Input
          id="password"
          type="password"
          autoComplete="current-password"
          placeholder="Enter your password"
          aria-invalid={Boolean(errors.password)}
          disabled={isSubmitting}
          {...register('password')}
        />
        {errors.password ? (
          <p className="text-destructive text-xs">{errors.password.message}</p>
        ) : null}
      </div>

      <Button type="submit" className="h-10 w-full" disabled={isSubmitting}>
        {isSubmitting ? (
          <>
            <Loader2Icon className="animate-spin" />
            Signing in…
          </>
        ) : (
          'Sign in'
        )}
      </Button>
    </form>
  );
}
