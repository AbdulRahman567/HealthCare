import { useMutation } from '@tanstack/react-query';

import { authApi } from '@/features/auth/api/auth-api';
import type { ForgotPasswordPayload } from '@/features/auth/types/auth.types';

export function useForgotPasswordMutation() {
  return useMutation({
    mutationKey: ['auth', 'forgot-password'],
    mutationFn: (payload: ForgotPasswordPayload) => authApi.forgotPassword(payload),
  });
}
