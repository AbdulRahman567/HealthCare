import { registerHospitalSchema } from '@/features/auth/validation/register-hospital-schema';

const validPayload = {
  hospitalName: 'General Hospital',
  hospitalEmail: 'contact@hospital.com',
  hospitalPhone: '+1-555-0100',
  hospitalAddress: '123 Main St',
  subscriptionPlan: 'STANDARD' as const,
  adminFirstName: 'Jane',
  adminLastName: 'Admin',
  adminEmail: 'admin@hospital.com',
  adminPassword: 'StrongPass1!ab',
  adminPhone: '+1-555-0199',
};

describe('registerHospitalSchema', () => {
  it('accepts a valid, fully populated payload', () => {
    const result = registerHospitalSchema.safeParse(validPayload);
    expect(result.success).toBe(true);
  });

  it('accepts a payload without optional phone/address', () => {
    const result = registerHospitalSchema.safeParse({
      hospitalName: 'General Hospital',
      hospitalEmail: 'contact@hospital.com',
      subscriptionPlan: 'BASIC',
      adminFirstName: 'Jane',
      adminLastName: 'Admin',
      adminEmail: 'admin@hospital.com',
      adminPassword: 'StrongPass1!ab',
    });

    expect(result.success).toBe(true);
  });

  it('rejects a hospital name shorter than 2 characters', () => {
    const result = registerHospitalSchema.safeParse({
      ...validPayload,
      hospitalName: 'G',
    });

    expect(result.success).toBe(false);
  });

  it('rejects a hospital name over 200 characters', () => {
    const result = registerHospitalSchema.safeParse({
      ...validPayload,
      hospitalName: 'a'.repeat(201),
    });

    expect(result.success).toBe(false);
  });

  it('rejects a malformed hospital email', () => {
    const result = registerHospitalSchema.safeParse({
      ...validPayload,
      hospitalEmail: 'not-an-email',
    });

    expect(result.success).toBe(false);
  });

  it('rejects a hospital phone number over 30 characters', () => {
    const result = registerHospitalSchema.safeParse({
      ...validPayload,
      hospitalPhone: '1'.repeat(31),
    });

    expect(result.success).toBe(false);
  });

  it('rejects an address over 500 characters', () => {
    const result = registerHospitalSchema.safeParse({
      ...validPayload,
      hospitalAddress: 'a'.repeat(501),
    });

    expect(result.success).toBe(false);
  });

  it('rejects an invalid subscription plan', () => {
    const result = registerHospitalSchema.safeParse({
      ...validPayload,
      subscriptionPlan: 'GOLD',
    });

    expect(result.success).toBe(false);
  });

  it('rejects a missing subscription plan', () => {
    const withoutPlan = {
      hospitalName: validPayload.hospitalName,
      hospitalEmail: validPayload.hospitalEmail,
      hospitalPhone: validPayload.hospitalPhone,
      hospitalAddress: validPayload.hospitalAddress,
      adminFirstName: validPayload.adminFirstName,
      adminLastName: validPayload.adminLastName,
      adminEmail: validPayload.adminEmail,
      adminPassword: validPayload.adminPassword,
      adminPhone: validPayload.adminPhone,
    };
    const result = registerHospitalSchema.safeParse(withoutPlan);

    expect(result.success).toBe(false);
  });

  it('rejects a weak admin password', () => {
    const result = registerHospitalSchema.safeParse({
      ...validPayload,
      adminPassword: 'weak',
    });

    expect(result.success).toBe(false);
  });

  it('rejects missing admin fields', () => {
    const result = registerHospitalSchema.safeParse({
      hospitalName: 'General Hospital',
      hospitalEmail: 'contact@hospital.com',
      subscriptionPlan: 'BASIC',
    });

    expect(result.success).toBe(false);
  });
});
