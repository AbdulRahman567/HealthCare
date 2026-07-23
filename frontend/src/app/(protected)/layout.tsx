import { ProtectedLayout } from '@/features/authorization/components/protected-layout';

export default function ProtectedAppLayout({ children }: { children: React.ReactNode }) {
  return <ProtectedLayout>{children}</ProtectedLayout>;
}
