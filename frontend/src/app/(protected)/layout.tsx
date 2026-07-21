import { RouteGuard } from '@/features/auth/components/route-guard';
import { ProtectedShell } from '@/features/auth/components/protected-shell';

export default function ProtectedLayout({ children }: { children: React.ReactNode }) {
  return (
    <RouteGuard>
      <ProtectedShell>{children}</ProtectedShell>
    </RouteGuard>
  );
}
