import { registerHospitalSchema } from '@/features/auth/validation/register-hospital-schema';

describe('registerHospitalSchema', () => {
  it('accepts a valid, fully populated payload', () => {
    const result = registerHospitalSchema.safeParse({
      hospitalName: 'General Hospital',
      email: 'contact@hospital.com',
      phone: '+1-555-0100',
      address: '123 Main St',
      subscriptionPlan: 'STANDARD',
    });

    expect(result.success).toBe(true);
  });

  it('accepts a payload without optional phone/address', () => {
    const result = registerHospitalSchema.safeParse({
      hospitalName: 'General Hospital',
      email: 'contact@hospital.com',
      subscriptionPlan: 'BASIC',
    });

    expect(result.success).toBe(true);
  });

  it('rejects a hospital name shorter than 2 characters', () => {
    const result = registerHospitalSchema.safeParse({
      hospitalName: 'G',
      email: 'contact@hospital.com',
      subscriptionPlan: 'BASIC',
    });

    expect(result.success).toBe(false);
  });

  it('rejects a hospital name over 200 characters', () => {
    const result = registerHospitalSchema.safeParse({
      hospitalName: 'a'.repeat(201),
      email: 'contact@hospital.com',
      subscriptionPlan: 'BASIC',
    });

    expect(result.success).toBe(false);
  });

  it('rejects a malformed email', () => {
    const result = registerHospitalSchema.safeParse({
      hospitalName: 'General Hospital',
      email: 'not-an-email',
      subscriptionPlan: 'BASIC',
    });

    expect(result.success).toBe(false);
  });

  it('rejects a phone number over 30 characters', () => {
    const result = registerHospitalSchema.safeParse({
      hospitalName: 'General Hospital',
      email: 'contact@hospital.com',
      phone: '1'.repeat(31),
      subscriptionPlan: 'BASIC',
    });

    expect(result.success).toBe(false);
  });

  it('rejects an address over 500 characters', () => {
    const result = registerHospitalSchema.safeParse({
      hospitalName: 'General Hospital',
      email: 'contact@hospital.com',
      address: 'a'.repeat(501),
      subscriptionPlan: 'BASIC',
    });

    expect(result.success).toBe(false);
  });

  it('rejects an invalid subscription plan', () => {
    const result = registerHospitalSchema.safeParse({
      hospitalName: 'General Hospital',
      email: 'contact@hospital.com',
      subscriptionPlan: 'GOLD',
    });

    expect(result.success).toBe(false);
  });

  it('rejects a missing subscription plan', () => {
    const result = registerHospitalSchema.safeParse({
      hospitalName: 'General Hospital',
      email: 'contact@hospital.com',
    });

    expect(result.success).toBe(false);
  });
});
