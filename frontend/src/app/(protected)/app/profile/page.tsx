import type { Metadata } from 'next';

import { ProfilePanel } from '@/features/auth/components/profile-panel';

export const metadata: Metadata = {
  title: 'Profile | Healthcare HMS',
  description: 'Authenticated user profile',
};

export default function ProfilePage() {
  return (
    <div className="mx-auto max-w-2xl space-y-4">
      <h1 className="text-2xl font-semibold tracking-tight">Profile</h1>
      <ProfilePanel />
    </div>
  );
}
