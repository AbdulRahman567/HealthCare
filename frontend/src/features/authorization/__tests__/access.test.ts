import { hasAll, hasAny, matchesAccess } from '@/features/authorization/lib/access';

describe('authorization access helpers', () => {
  it('treats empty requirements as allowed', () => {
    expect(hasAny(['A'], [])).toBe(true);
    expect(hasAll(['A'], undefined)).toBe(true);
    expect(matchesAccess(['ADMIN'], ['P1'], undefined)).toBe(true);
  });

  it('matches any / all permission sets', () => {
    expect(hasAny(['HOSPITAL_READ', 'USER_READ'], ['USER_DELETE', 'USER_READ'])).toBe(true);
    expect(hasAny(['HOSPITAL_READ'], ['USER_READ'])).toBe(false);
    expect(hasAll(['A', 'B', 'C'], ['A', 'C'])).toBe(true);
    expect(hasAll(['A', 'B'], ['A', 'C'])).toBe(false);
  });

  it('ANDs role and permission requirements', () => {
    expect(
      matchesAccess(['HOSPITAL_ADMIN'], ['HOSPITAL_READ'], {
        roles: ['HOSPITAL_ADMIN'],
        permissions: ['HOSPITAL_READ'],
      }),
    ).toBe(true);

    expect(
      matchesAccess(['DOCTOR'], ['HOSPITAL_READ'], {
        roles: ['HOSPITAL_ADMIN'],
        permissions: ['HOSPITAL_READ'],
      }),
    ).toBe(false);
  });
});
