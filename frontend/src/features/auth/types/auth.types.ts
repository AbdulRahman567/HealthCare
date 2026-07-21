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

export type RefreshTokenPayload = {
  refreshToken: string;
};

export type SubscriptionPlan = 'BASIC' | 'STANDARD' | 'PREMIUM' | 'ENTERPRISE';

export type TenantStatus = 'ACTIVE' | 'SUSPENDED' | 'INACTIVE';

export type HospitalRegistrationResponse = {
  tenantId: string;
  name: string;
  slug: string;
  email: string;
  phone: string | null;
  address: string | null;
  subscriptionPlan: SubscriptionPlan;
  status: TenantStatus;
  createdAt: string;
};

export type LoginPayload = {
  email: string;
  password: string;
};

export type RegisterHospitalPayload = {
  hospitalName: string;
  email: string;
  phone?: string;
  address?: string;
  subscriptionPlan?: SubscriptionPlan;
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
