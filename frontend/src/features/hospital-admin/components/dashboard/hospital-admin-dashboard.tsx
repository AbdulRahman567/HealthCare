'use client';

import Link from 'next/link';
import {
  Building2Icon,
  MailPlusIcon,
  StethoscopeIcon,
  UsersIcon,
  type LucideIcon,
} from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { Can } from '@/features/authorization/components/can';
import { Permissions } from '@/features/authorization/constants/permissions';
import { useHospitalDashboardStats } from '@/features/hospital-admin/hooks/use-hospital-dashboard-stats';
import { useSession } from '@/providers/session-provider';
import { cn } from '@/lib/utils';

type StatCardProps = {
  title: string;
  value: number | null;
  href: string;
  icon: LucideIcon;
  description: string;
  loading?: boolean;
};

function StatCard({ title, value, href, icon: Icon, description, loading }: StatCardProps) {
  if (value === null && !loading) {
    return null;
  }

  return (
    <Card size="sm" className="transition-colors hover:bg-muted/30">
      <CardHeader className="flex flex-row items-start justify-between gap-3">
        <div className="space-y-1">
          <CardDescription>{title}</CardDescription>
          {loading ? (
            <Skeleton className="h-8 w-16" />
          ) : (
            <CardTitle className="text-3xl tabular-nums">{value ?? 0}</CardTitle>
          )}
        </div>
        <div className="bg-primary/10 text-primary rounded-lg p-2">
          <Icon className="size-4" />
        </div>
      </CardHeader>
      <CardContent className="flex items-end justify-between gap-3">
        <p className="text-muted-foreground text-xs text-pretty">{description}</p>
        <Button nativeButton={false} render={<Link href={href} />} variant="outline" size="sm">
          Open
        </Button>
      </CardContent>
    </Card>
  );
}

/**
 * Hospital administration overview with live totals from Phase 4 APIs.
 */
export function HospitalAdminDashboard() {
  const { user } = useSession();
  const stats = useHospitalDashboardStats();

  return (
    <section className="space-y-4">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div className="space-y-1">
          <h2 className="text-lg font-medium">Hospital administration</h2>
          <p className="text-muted-foreground text-sm text-pretty">
            {user
              ? `Welcome back, ${user.firstName}. Manage departments, staff, users, and invitations.`
              : 'Manage departments, staff, users, and invitations.'}
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Can permissions={[Permissions.USER_CREATE]}>
            <Button nativeButton={false} render={<Link href="/app/invitations" />} size="sm">
              <MailPlusIcon data-icon="inline-start" />
              Invite staff
            </Button>
          </Can>
          <Can permissions={[Permissions.DEPARTMENT_CREATE]}>
            <Button
              nativeButton={false}
              render={<Link href="/app/departments" />}
              variant="outline"
              size="sm"
            >
              Add department
            </Button>
          </Can>
        </div>
      </div>

      <div className={cn('grid gap-4', 'grid-cols-1 sm:grid-cols-2 xl:grid-cols-4')}>
        <Can permissions={[Permissions.DEPARTMENT_READ]}>
          <StatCard
            title="Departments"
            value={stats.departments}
            href="/app/departments"
            icon={Building2Icon}
            description="Active organizational units"
            loading={stats.isLoading}
          />
        </Can>
        <Can permissions={[Permissions.DOCTOR_READ, Permissions.STAFF_READ]} mode="any">
          <StatCard
            title="Staff profiles"
            value={stats.staff}
            href="/app/staff"
            icon={StethoscopeIcon}
            description="Employment records across roles"
            loading={stats.isLoading}
          />
        </Can>
        <Can permissions={[Permissions.USER_READ]}>
          <StatCard
            title="Users"
            value={stats.users}
            href="/app/users"
            icon={UsersIcon}
            description="Accounts in this hospital tenant"
            loading={stats.isLoading}
          />
        </Can>
        <Can permissions={[Permissions.USER_READ]}>
          <StatCard
            title="Pending invites"
            value={stats.pendingInvitations}
            href="/app/invitations"
            icon={MailPlusIcon}
            description="Awaiting acceptance"
            loading={stats.isLoading}
          />
        </Can>
      </div>
    </section>
  );
}
