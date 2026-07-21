import { loginSchema } from '@/features/auth/validation/login-schema';

describe('loginSchema', () => {
  it('accepts a valid email and password', () => {
    const result = loginSchema.safeParse({
      email: 'admin@hospital.com',
      password: 'Sup3rSecret!',
    });

    expect(result.success).toBe(true);
  });

  it('trims whitespace from the email', () => {
    const result = loginSchema.safeParse({
      email: '  admin@hospital.com  ',
      password: 'Sup3rSecret!',
    });

    expect(result.success).toBe(true);
    if (result.success) {
      expect(result.data.email).toBe('admin@hospital.com');
    }
  });

  it('rejects an empty email', () => {
    const result = loginSchema.safeParse({ email: '', password: 'Sup3rSecret!' });

    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.error.issues.some((issue) => issue.path[0] === 'email')).toBe(true);
    }
  });

  it('rejects a malformed email', () => {
    const result = loginSchema.safeParse({ email: 'not-an-email', password: 'Sup3rSecret!' });

    expect(result.success).toBe(false);
  });

  it('rejects an email over 255 characters', () => {
    const longEmail = `${'a'.repeat(250)}@x.com`;
    const result = loginSchema.safeParse({ email: longEmail, password: 'Sup3rSecret!' });

    expect(result.success).toBe(false);
  });

  it('rejects an empty password', () => {
    const result = loginSchema.safeParse({ email: 'admin@hospital.com', password: '' });

    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.error.issues.some((issue) => issue.path[0] === 'password')).toBe(true);
    }
  });

  it('rejects a password over 128 characters', () => {
    const result = loginSchema.safeParse({
      email: 'admin@hospital.com',
      password: 'a'.repeat(129),
    });

    expect(result.success).toBe(false);
  });
});
