'use client';

import {
  CalendarDaysIcon,
  ClipboardListIcon,
  ListIcon,
  PlusIcon,
} from 'lucide-react';
import Link from 'next/link';
import { useMemo } from 'react';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Can } from '@/features/authorization/components/can';
import { Permissions } from '@/features/authorization/constants/permissions';
import { AppointmentSubnav } from '@/features/appointments/components/shared/appointment-subnav';
import { useAppointmentsQuery } from '@/features/appointments/hooks/use-appointments';
import { formatAppointmentSlot, formatTimeLabel } from '@/features/appointments/lib/appointment-format';
import { useAppointmentLookups } from '@/features/appointments/hooks/use-appointment-lookups';
import { AdminPageHeader } from '@/features/hospital-admin/components/shared/admin-page-header';
import { StatusBadge } from '@/features/hospital-admin/components/shared/status-badge';
import { getErrorMessage } from '@/lib/api-error';
import { formatEnumLabel } from '@/lib/page-query';

export function AppointmentDashboardPage() {
  const today = useMemo(() => new Date().toISOString().slice(0, 10), []);

  const todayQuery = useAppointmentsQuery({
    fromDate: today,
    toDate: today,
    page: 0,
    size: 8,
    sort: ['startTime,asc'],
  });

  const upcomingQuery = useAppointmentsQuery({
    fromDate: today,
    status: 'SCHEDULED',
    page: 0,
    size: 5,
    sort: ['appointmentDate,asc', 'startTime,asc'],
  });

  const confirmedQuery = useAppointmentsQuery({
    fromDate: today,
    toDate: today,
    status: 'CONFIRMED',
    page: 0,
    size: 1,
  });

  const rows = todayQuery.data?.content ?? [];
  const lookups = useAppointmentLookups(rows);

  const stats = [
    {
      label: 'Today',
      value: todayQuery.data?.totalElements ?? 0,
      hint: 'Appointments scheduled for today',
    },
    {
      label: 'Confirmed today',
      value: confirmedQuery.data?.totalElements ?? 0,
      hint: 'Patients confirmed for today',
    },
    {
      label: 'Upcoming scheduled',
      value: upcomingQuery.data?.totalElements ?? 0,
      hint: 'Future SCHEDULED appointments',
    },
  ];

  return (
    <div className="mx-auto max-w-6xl space-y-6">
      <AdminPageHeader
        title="Appointments"
        description="Schedule visits, manage the daily queue, and review the hospital calendar."
        actions={
          <Can permissions={[Permissions.APPOINTMENT_CREATE]}>
            <Button nativeButton={false} render={<Link href="/app/appointments/new" />}>
              <PlusIcon data-icon="inline-start" />
              Book appointment
            </Button>
          </Can>
        }
      />

      <AppointmentSubnav />

      <div className="grid gap-4 sm:grid-cols-3">
        {stats.map((stat) => (
          <Card key={stat.label}>
            <CardHeader className="pb-2">
              <CardDescription>{stat.label}</CardDescription>
              <CardTitle className="text-3xl tabular-nums">{stat.value}</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-muted-foreground text-xs">{stat.hint}</p>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid gap-4 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardHeader className="flex flex-row items-center justify-between gap-2 space-y-0">
            <div>
              <CardTitle className="text-base">Today&apos;s schedule</CardTitle>
              <CardDescription>{today}</CardDescription>
            </div>
            <Button
              nativeButton={false}
              variant="outline"
              size="sm"
              render={<Link href="/app/appointments/list" />}
            >
              View list
            </Button>
          </CardHeader>
          <CardContent>
            {todayQuery.isError ? (
              <p className="text-destructive text-sm">{getErrorMessage(todayQuery.error)}</p>
            ) : todayQuery.isLoading ? (
              <p className="text-muted-foreground text-sm">Loading…</p>
            ) : rows.length === 0 ? (
              <p className="text-muted-foreground text-sm">No appointments for today.</p>
            ) : (
              <ul className="divide-y rounded-lg border">
                {rows.map((appointment) => (
                  <li key={appointment.id}>
                    <Link
                      href={`/app/appointments/${appointment.id}`}
                      className="hover:bg-muted/40 flex flex-col gap-1 px-3 py-3 transition-colors sm:flex-row sm:items-center sm:justify-between"
                    >
                      <div className="min-w-0 space-y-0.5">
                        <p className="truncate font-medium">
                          {lookups.patientNameById.get(appointment.patientId) ?? 'Patient'}
                          <span className="text-muted-foreground font-normal">
                            {' '}
                            · {appointment.appointmentNumber}
                          </span>
                        </p>
                        <p className="text-muted-foreground truncate text-xs">
                          {formatTimeLabel(appointment.startTime)}–
                          {formatTimeLabel(appointment.endTime)}
                          {' · '}
                          {lookups.doctorNameById.get(appointment.doctorId) ?? 'Doctor'}
                          {' · '}
                          {formatEnumLabel(appointment.visitType)}
                        </p>
                      </div>
                      <StatusBadge status={appointment.status} />
                    </Link>
                  </li>
                ))}
              </ul>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">Quick links</CardTitle>
            <CardDescription>Jump to scheduling workflows</CardDescription>
          </CardHeader>
          <CardContent className="grid gap-2">
            <Button
              nativeButton={false}
              variant="outline"
              className="justify-start"
              render={<Link href="/app/appointments/list" />}
            >
              <ListIcon data-icon="inline-start" />
              Appointment list
            </Button>
            <Button
              nativeButton={false}
              variant="outline"
              className="justify-start"
              render={<Link href="/app/appointments/calendar" />}
            >
              <CalendarDaysIcon data-icon="inline-start" />
              Calendar view
            </Button>
            <Button
              nativeButton={false}
              variant="outline"
              className="justify-start"
              render={<Link href="/app/appointments/queue" />}
            >
              <ClipboardListIcon data-icon="inline-start" />
              Doctor queue
            </Button>
            <Can permissions={[Permissions.APPOINTMENT_CREATE]}>
              <Button
                nativeButton={false}
                className="justify-start"
                render={<Link href="/app/appointments/new" />}
              >
                <PlusIcon data-icon="inline-start" />
                Book appointment
              </Button>
            </Can>
          </CardContent>
        </Card>
      </div>

      {(upcomingQuery.data?.content?.length ?? 0) > 0 ? (
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Upcoming scheduled</CardTitle>
            <CardDescription>Next scheduled visits across the hospital</CardDescription>
          </CardHeader>
          <CardContent className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-muted-foreground border-b text-left">
                  <th className="px-2 py-2 font-medium">When</th>
                  <th className="px-2 py-2 font-medium">Number</th>
                  <th className="px-2 py-2 font-medium">Type</th>
                  <th className="px-2 py-2 font-medium">Status</th>
                </tr>
              </thead>
              <tbody>
                {(upcomingQuery.data?.content ?? []).map((appointment) => (
                  <tr key={appointment.id} className="border-b last:border-0">
                    <td className="px-2 py-2">
                      <Link
                        href={`/app/appointments/${appointment.id}`}
                        className="hover:underline"
                      >
                        {formatAppointmentSlot(
                          appointment.appointmentDate,
                          appointment.startTime,
                          appointment.endTime,
                        )}
                      </Link>
                    </td>
                    <td className="px-2 py-2 font-mono text-xs">
                      {appointment.appointmentNumber}
                    </td>
                    <td className="px-2 py-2">{formatEnumLabel(appointment.appointmentType)}</td>
                    <td className="px-2 py-2">
                      <StatusBadge status={appointment.status} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </CardContent>
        </Card>
      ) : null}
    </div>
  );
}
