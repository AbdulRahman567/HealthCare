import { useMutation } from '@tanstack/react-query';

import { authApi } from '@/features/auth/api/auth-api';
import type { ResetPasswordPayload } from '@/features/auth/types/auth.types';

export function useResetPasswordMutation() {
  return useMutation({
    mutationKey: ['auth', 'reset-password'],
    mutationFn: (payload: ResetPasswordPayload) => authApi.resetPassword(payload),
  });
}
