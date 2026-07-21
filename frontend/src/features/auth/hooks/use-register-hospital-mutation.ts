import { useMutation } from '@tanstack/react-query';

import { authApi } from '@/features/auth/api/auth-api';
import type { RegisterHospitalPayload } from '@/features/auth/types/auth.types';

export function useRegisterHospitalMutation() {
  return useMutation({
    mutationKey: ['auth', 'register-hospital'],
    mutationFn: (payload: RegisterHospitalPayload) => authApi.registerHospital(payload),
  });
}
