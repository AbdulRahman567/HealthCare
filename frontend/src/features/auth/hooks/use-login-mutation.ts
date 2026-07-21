import { useMutation } from '@tanstack/react-query';

import { authApi } from '@/features/auth/api/auth-api';
import type { LoginPayload } from '@/features/auth/types/auth.types';

export function useLoginMutation() {
  return useMutation({
    mutationKey: ['auth', 'login'],
    mutationFn: (payload: LoginPayload) => authApi.login(payload),
  });
}
