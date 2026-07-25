'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2Icon } from 'lucide-react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useEffect, useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { AuthFormMessage } from '@/features/auth/components/auth-form-message';
import {
  invitationsApi,
  type InvitationPreviewResponse,
} from '@/features/hospital-admin/api/invitations-api';
import {
  acceptInvitationSchema,
  type AcceptInvitationFormValues,
} from '@/features/hospital-admin/validation/accept-invitation-schema';
import { getErrorMessage } from '@/lib/api-error';
import { formatEnumLabel } from '@/lib/page-query';

function readInvitationToken(): string {
  if (typeof window === 'undefined') {
    return '';
  }
  const hash = window.location.hash.replace(/^#/, '');
  if (hash) {
    const fromHash = new URLSearchParams(hash).get('token')?.trim();
    if (fromHash) {
      return fromHash;
    }
  }
  return new URLSearchParams(window.location.search).get('token')?.trim() ?? '';
}

/**
 * Public invitee onboarding: preview → accept (create account) or reject.
 * Token is read from URL fragment (preferred) or query (legacy email links).
 */
export function AcceptInvitationForm() {
  const router = useRouter();
  const [token, setToken] = useState('');
  const [preview, setPreview] = useState<InvitationPreviewResponse | null>(null);
  const [previewError, setPreviewError] = useState<string | null>(null);
  const [previewLoading, setPreviewLoading] = useState(true);
  const [rejecting, setRejecting] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<AcceptInvitationFormValues>({
    resolver: zodResolver(acceptInvitationSchema),
    defaultValues: {
      firstName: '',
      lastName: '',
      password: '',
      confirmPassword: '',
      phone: '',
    },
  });

  useEffect(() => {
    const resolved = readInvitationToken();
    setToken(resolved);
    if (!resolved || resolved.length < 32) {
      setPreviewLoading(false);
      setPreviewError('This invitation link is missing or incomplete.');
      return;
    }

    let cancelled = false;
    void (async () => {
      try {
        const data = await invitationsApi.preview(resolved);
        if (cancelled) {
          return;
        }
        setPreview(data);
        reset({
          firstName: data.firstName ?? '',
          lastName: data.lastName ?? '',
          password: '',
          confirmPassword: '',
          phone: '',
        });
        if (data.expired) {
          setPreviewError('This invitation has expired. Ask your hospital admin to resend it.');
        }
      } catch (error) {
        if (!cancelled) {
          setPreviewError(getErrorMessage(error, 'Invitation is invalid or no longer available'));
        }
      } finally {
        if (!cancelled) {
          setPreviewLoading(false);
        }
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [reset]);

  const hospitalLabel = useMemo(() => preview?.hospitalName ?? 'the hospital', [preview]);

  const onSubmit = handleSubmit(async (values) => {
    try {
      await invitationsApi.accept({
        token,
        firstName: values.firstName.trim(),
        lastName: values.lastName.trim(),
        password: values.password,
        phone: values.phone?.trim() ? values.phone.trim() : null,
      });
      toast.success('Account created. You can sign in now.');
      router.replace('/login');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to accept invitation'));
    }
  });

  const onReject = async () => {
    setRejecting(true);
    try {
      await invitationsApi.reject({ token });
      toast.success('Invitation rejected.');
      router.replace('/login');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to reject invitation'));
    } finally {
      setRejecting(false);
    }
  };

  if (previewLoading) {
    return <p className="text-muted-foreground text-sm">Loading invitation…</p>;
  }

  if (previewError || !preview) {
    return (
      <div className="space-y-5">
        <AuthFormMessage
          variant="error"
          title="Invitation unavailable"
          description={previewError ?? 'This invitation cannot be used.'}
        />
        <Link
          href="/login"
          className="bg-primary text-primary-foreground hover:bg-primary/80 inline-flex h-10 w-full items-center justify-center rounded-lg text-sm font-medium"
        >
          Back to sign in
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-5">
      <div className="rounded-lg border bg-muted/40 px-4 py-3 text-sm">
        <p>
          You are invited to join <strong>{hospitalLabel}</strong> as{' '}
          <strong>{formatEnumLabel(preview.roleType)}</strong>.
        </p>
        <p className="text-muted-foreground mt-1">{preview.email}</p>
      </div>

      <form onSubmit={onSubmit} className="space-y-4" noValidate>
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="space-y-2">
            <Label htmlFor="firstName">First name</Label>
            <Input id="firstName" autoComplete="given-name" {...register('firstName')} />
            {errors.firstName ? (
              <p className="text-destructive text-xs">{errors.firstName.message}</p>
            ) : null}
          </div>
          <div className="space-y-2">
            <Label htmlFor="lastName">Last name</Label>
            <Input id="lastName" autoComplete="family-name" {...register('lastName')} />
            {errors.lastName ? (
              <p className="text-destructive text-xs">{errors.lastName.message}</p>
            ) : null}
          </div>
        </div>

        <div className="space-y-2">
          <Label htmlFor="phone">Phone (optional)</Label>
          <Input id="phone" autoComplete="tel" {...register('phone')} />
          {errors.phone ? <p className="text-destructive text-xs">{errors.phone.message}</p> : null}
        </div>

        <div className="space-y-2">
          <Label htmlFor="password">Password</Label>
          <Input id="password" type="password" autoComplete="new-password" {...register('password')} />
          {errors.password ? (
            <p className="text-destructive text-xs">{errors.password.message}</p>
          ) : null}
        </div>

        <div className="space-y-2">
          <Label htmlFor="confirmPassword">Confirm password</Label>
          <Input
            id="confirmPassword"
            type="password"
            autoComplete="new-password"
            {...register('confirmPassword')}
          />
          {errors.confirmPassword ? (
            <p className="text-destructive text-xs">{errors.confirmPassword.message}</p>
          ) : null}
        </div>

        <Button type="submit" className="w-full" disabled={isSubmitting || rejecting}>
          {isSubmitting ? (
            <>
              <Loader2Icon className="size-4 animate-spin" />
              Creating account…
            </>
          ) : (
            'Accept invitation'
          )}
        </Button>
      </form>

      <Button
        type="button"
        variant="outline"
        className="w-full"
        disabled={isSubmitting || rejecting}
        onClick={() => void onReject()}
      >
        {rejecting ? 'Rejecting…' : 'Reject invitation'}
      </Button>
    </div>
  );
}
