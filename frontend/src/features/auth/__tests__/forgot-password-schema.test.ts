import { forgotPasswordSchema } from '@/features/auth/validation/forgot-password-schema';

describe('forgotPasswordSchema', () => {
  it('accepts a valid email', () => {
    const result = forgotPasswordSchema.safeParse({ email: 'user@hospital.com' });

    expect(result.success).toBe(true);
  });

  it('trims whitespace from the email', () => {
    const result = forgotPasswordSchema.safeParse({ email: '  user@hospital.com  ' });

    expect(result.success).toBe(true);
    if (result.success) {
      expect(result.data.email).toBe('user@hospital.com');
    }
  });

  it('rejects an empty email', () => {
    const result = forgotPasswordSchema.safeParse({ email: '' });

    expect(result.success).toBe(false);
  });

  it('rejects a malformed email', () => {
    const result = forgotPasswordSchema.safeParse({ email: 'nope' });

    expect(result.success).toBe(false);
  });

  it('rejects an email over 255 characters', () => {
    const longEmail = `${'a'.repeat(250)}@x.com`;
    const result = forgotPasswordSchema.safeParse({ email: longEmail });

    expect(result.success).toBe(false);
  });
});
