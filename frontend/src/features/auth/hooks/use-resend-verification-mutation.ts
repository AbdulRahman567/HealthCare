import { useMutation } from '@tanstack/react-query';

import { authApi } from '@/features/auth/api/auth-api';
import type { ResendVerificationPayload } from '@/features/auth/types/auth.types';

export function useResendVerificationMutation() {
  return useMutation({
    mutationKey: ['auth', 'resend-verification'],
    mutationFn: (payload: ResendVerificationPayload) => authApi.resendVerification(payload),
  });
}
