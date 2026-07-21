'use client';

import { Loader2Icon } from 'lucide-react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useEffect, useMemo, useRef } from 'react';

import { authApi } from '@/features/auth/api/auth-api';
import { ApiClientError } from '@/lib/api-error';

/**
 * Consumes the verification token from the email link and routes to success or failed pages.
 */
export function VerifyEmailHandler() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const token = useMemo(() => searchParams.get('token')?.trim() ?? '', [searchParams]);
  const startedRef = useRef(false);

  useEffect(() => {
    if (startedRef.current) {
      return;
    }
    startedRef.current = true;

    if (!token || token.length < 32) {
      router.replace('/verify-email/failed?reason=missing');
      return;
    }

    void authApi
      .verifyEmail({ token })
      .then(() => {
        router.replace('/verify-email/success');
      })
      .catch((error: unknown) => {
        const reason =
          error instanceof ApiClientError && error.errorCode === 'AUTH_EXPIRED_TOKEN'
            ? 'expired'
            : error instanceof ApiClientError && error.errorCode === 'AUTH_INVALID_TOKEN'
              ? 'invalid'
              : 'failed';
        router.replace(`/verify-email/failed?reason=${reason}`);
      });
  }, [router, token]);

  return (
    <div className="flex flex-col items-center gap-3 py-6 text-center">
      <Loader2Icon className="text-primary size-8 animate-spin" />
      <p className="text-muted-foreground text-sm">Verifying your email address…</p>
    </div>
  );
}
