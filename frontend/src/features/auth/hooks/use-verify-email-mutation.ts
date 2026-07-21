import { useMutation } from '@tanstack/react-query';

import { authApi } from '@/features/auth/api/auth-api';
import type { VerifyEmailPayload } from '@/features/auth/types/auth.types';

export function useVerifyEmailMutation() {
  return useMutation({
    mutationKey: ['auth', 'verify-email'],
    mutationFn: (payload: VerifyEmailPayload) => authApi.verifyEmail(payload),
  });
}
