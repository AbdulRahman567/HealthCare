export type ApiSuccessResponse<T> = {
  success: true;
  message: string;
  data: T;
  timestamp: string;
};

export type ApiErrorDetail = {
  field?: string;
  message: string;
};

export type ApiErrorBody = {
  success: false;
  message: string;
  errorCode?: string;
  errors?: ApiErrorDetail[];
  timestamp?: string;
  path?: string;
};

export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'LOCKED' | 'PENDING';

export type UserProfile = {
  id: string;
  tenantId: string | null;
  firstName: string;
  lastName: string;
  email: string;
  phone: string | null;
  emailVerified: boolean;
  emailVerifiedAt: string | null;
  status: UserStatus;
  roles: string[];
  permissions: string[];
  lastLoginAt: string | null;
  createdAt: string;
};

export type AuthResponse = {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInSeconds: number;
  refreshExpiresInSeconds: number;
  user: UserProfile;
};

export type SubscriptionPlan = 'BASIC' | 'STANDARD' | 'PREMIUM' | 'ENTERPRISE';

/** Single-page registration payload (Phase 7). Only a pending record is created on submit. */
export type RegistrationPayload = {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phone?: string;
  hospitalName: string;
  hospitalEmail: string;
  hospitalPhone?: string;
  hospitalAddress?: string;
  subscriptionPlan: SubscriptionPlan;
};

export type PendingRegistrationResponse = {
  email: string;
  expiresInMinutes: number;
};

export type TenantStatus = 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'INACTIVE';

export type HospitalStatus = 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'INACTIVE';

export type HospitalRegistrationResponse = {
  tenantId: string;
  tenantSlug: string;
  tenantStatus: TenantStatus;
  hospitalId: string;
  hospitalName: string;
  hospitalCode: string;
  hospitalStatus: HospitalStatus;
  defaultHospital: boolean;
  hospitalEmail: string;
  hospitalPhone: string | null;
  hospitalAddress: string | null;
  subscriptionPlan: SubscriptionPlan;
  trialEndsAt: string | null;
  adminUserId: string;
  adminEmail: string;
  adminEmailVerified: boolean;
  provisionedRoles: string[];
  createdAt: string;
};

export type LoginPayload = {
  email: string;
  password: string;
};

export type ForgotPasswordPayload = {
  email: string;
};

export type ResetPasswordPayload = {
  token: string;
  newPassword: string;
};

export type VerifyEmailPayload = {
  token: string;
};

export type ResendVerificationPayload = {
  email: string;
};
