import { resendVerificationSchema } from '@/features/auth/validation/resend-verification-schema';

describe('resendVerificationSchema', () => {
  it('accepts a valid email', () => {
    const result = resendVerificationSchema.safeParse({ email: 'user@hospital.com' });

    expect(result.success).toBe(true);
  });

  it('trims whitespace from the email', () => {
    const result = resendVerificationSchema.safeParse({ email: '  user@hospital.com  ' });

    expect(result.success).toBe(true);
    if (result.success) {
      expect(result.data.email).toBe('user@hospital.com');
    }
  });

  it('rejects an empty email', () => {
    const result = resendVerificationSchema.safeParse({ email: '' });

    expect(result.success).toBe(false);
  });

  it('rejects a malformed email', () => {
    const result = resendVerificationSchema.safeParse({ email: 'nope' });

    expect(result.success).toBe(false);
  });

  it('rejects an email over 255 characters', () => {
    const longEmail = `${'a'.repeat(250)}@x.com`;
    const result = resendVerificationSchema.safeParse({ email: longEmail });

    expect(result.success).toBe(false);
  });
});
