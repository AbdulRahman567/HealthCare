'use client';

import { useSearchParams } from 'next/navigation';
import { Suspense } from 'react';

function FailedReasonText() {
  const searchParams = useSearchParams();
  const reason = searchParams.get('reason');

  const message =
    reason === 'expired'
      ? 'This verification link has expired. Request a new one to continue.'
      : reason === 'missing'
        ? 'This page was opened without a verification token.'
        : reason === 'invalid'
          ? 'This verification link is invalid or has already been used.'
          : 'Something went wrong while verifying your email.';

  return <p className="text-muted-foreground mt-4 text-sm">{message}</p>;
}

export function VerifyEmailFailedDetails() {
  return (
    <Suspense fallback={null}>
      <FailedReasonText />
    </Suspense>
  );
}
