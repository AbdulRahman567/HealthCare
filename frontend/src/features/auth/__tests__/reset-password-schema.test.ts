import { resetPasswordSchema } from '@/features/auth/validation/reset-password-schema';

const STRONG_PASSWORD = 'Sup3r-Secret!!';

describe('resetPasswordSchema', () => {
  it('accepts a strong password with matching confirmation', () => {
    const result = resetPasswordSchema.safeParse({
      newPassword: STRONG_PASSWORD,
      confirmPassword: STRONG_PASSWORD,
    });

    expect(result.success).toBe(true);
  });

  it('rejects a password shorter than 12 characters', () => {
    const result = resetPasswordSchema.safeParse({
      newPassword: 'Short1!',
      confirmPassword: 'Short1!',
    });

    expect(result.success).toBe(false);
  });

  it('rejects a password over 128 characters', () => {
    const tooLong = `Aa1!${'a'.repeat(126)}`;
    const result = resetPasswordSchema.safeParse({
      newPassword: tooLong,
      confirmPassword: tooLong,
    });

    expect(result.success).toBe(false);
  });

  it('rejects a password missing an uppercase letter', () => {
    const result = resetPasswordSchema.safeParse({
      newPassword: 'lowercase123!!',
      confirmPassword: 'lowercase123!!',
    });

    expect(result.success).toBe(false);
  });

  it('rejects a password missing a lowercase letter', () => {
    const result = resetPasswordSchema.safeParse({
      newPassword: 'UPPERCASE123!!',
      confirmPassword: 'UPPERCASE123!!',
    });

    expect(result.success).toBe(false);
  });

  it('rejects a password missing a digit', () => {
    const result = resetPasswordSchema.safeParse({
      newPassword: 'NoDigitsHere!!',
      confirmPassword: 'NoDigitsHere!!',
    });

    expect(result.success).toBe(false);
  });

  it('rejects a password missing a special character', () => {
    const result = resetPasswordSchema.safeParse({
      newPassword: 'NoSpecialChar123',
      confirmPassword: 'NoSpecialChar123',
    });

    expect(result.success).toBe(false);
  });

  it('rejects mismatched confirmation with an error on confirmPassword', () => {
    const result = resetPasswordSchema.safeParse({
      newPassword: STRONG_PASSWORD,
      confirmPassword: 'Different123!!',
    });

    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.error.issues.some((issue) => issue.path[0] === 'confirmPassword')).toBe(true);
    }
  });

  it('rejects an empty confirmation', () => {
    const result = resetPasswordSchema.safeParse({
      newPassword: STRONG_PASSWORD,
      confirmPassword: '',
    });

    expect(result.success).toBe(false);
  });
});
