'use client';

import { Loader2Icon } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { toast } from 'sonner';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Can } from '@/features/authorization/components/can';
import { Permissions } from '@/features/authorization/constants/permissions';
import { availabilityApi } from '@/features/appointments/api/availability-api';
import { AppointmentSubnav } from '@/features/appointments/components/shared/appointment-subnav';
import { useAppointmentLookups } from '@/features/appointments/hooks/use-appointment-lookups';
import { useUsersQuery } from '@/features/hospital-admin/hooks/use-users';
import { AdminPageHeader } from '@/features/hospital-admin/components/shared/admin-page-header';
import { EmptyState } from '@/features/hospital-admin/components/shared/empty-state';
import { StatusBadge } from '@/features/hospital-admin/components/shared/status-badge';
import { getErrorMessage } from '@/lib/api-error';
import { formatPersonName } from '@/lib/page-query';

const WEEKDAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'] as const;

function todayIsoDate() {
  return new Date().toISOString().slice(0, 10);
}

export function AvailabilityPage() {
  const lookups = useAppointmentLookups([]);
  const usersQuery = useUsersQuery({
    page: 0,
    size: 100,
    status: 'ACTIVE',
    roleType: 'DOCTOR',
    sort: ['lastName,asc', 'firstName,asc'],
  });
  const [doctorId, setDoctorId] = useState('');
  const [effectiveFrom, setEffectiveFrom] = useState(todayIsoDate());
  const [startTime, setStartTime] = useState('09:00');
  const [endTime, setEndTime] = useState('17:00');
  const [maxPerDay, setMaxPerDay] = useState(20);
  const queryClient = useQueryClient();

  const doctorOptions = useMemo(() => {
    const userNameById = new Map<string, string>();
    for (const user of usersQuery.data?.content ?? []) {
      userNameById.set(user.id, formatPersonName(user.firstName, user.lastName));
    }
    return lookups.doctors.map((doctor) => ({
      id: doctor.id,
      label: userNameById.get(doctor.userId)
        ? `${userNameById.get(doctor.userId)} (${doctor.employeeCode})`
        : doctor.employeeCode,
    }));
  }, [lookups.doctors, usersQuery.data]);

  useEffect(() => {
    if (!doctorId && doctorOptions.length > 0) {
      setDoctorId(doctorOptions[0].id);
    }
  }, [doctorId, doctorOptions]);

  const schedulesQuery = useQuery({
    queryKey: ['appointments', 'schedules', doctorId],
    queryFn: () => availabilityApi.listSchedules(doctorId),
    enabled: Boolean(doctorId),
  });

  const createMutation = useMutation({
    mutationFn: () =>
      availabilityApi.createSchedule(doctorId, {
        name: 'Standard clinic hours',
        effectiveFrom,
        maxAppointmentsPerDay: maxPerDay,
        recurrenceType: 'WEEKLY',
        status: 'ACTIVE',
        windows: WEEKDAYS.map((dayOfWeek) => ({
          dayOfWeek,
          startTime: `${startTime}:00`,
          endTime: `${endTime}:00`,
        })),
        breaks: [],
      }),
    onSuccess: async () => {
      toast.success('Doctor schedule created');
      await queryClient.invalidateQueries({ queryKey: ['appointments', 'schedules', doctorId] });
    },
    onError: (error) => {
      toast.error(getErrorMessage(error, 'Unable to create schedule'));
    },
  });

  return (
    <div className="mx-auto max-w-6xl space-y-6">
      <AdminPageHeader
        title="Doctor availability"
        description="Publish recurring working hours so appointments can be booked against a doctor’s schedule."
      />
      <AppointmentSubnav />

      <div className="grid gap-3 rounded-xl border bg-card p-4 sm:grid-cols-2">
        <div className="space-y-2">
          <Label>Doctor</Label>
          <Select value={doctorId || undefined} onValueChange={(value) => setDoctorId(value ?? '')}>
            <SelectTrigger>
              <SelectValue placeholder="Select doctor" />
            </SelectTrigger>
            <SelectContent>
              {doctorOptions.map((option) => (
                <SelectItem key={option.id} value={option.id}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      {!doctorId ? (
        <EmptyState
          title="Select a doctor"
          description="Choose a doctor to view or publish schedules."
        />
      ) : (
        <>
          <Can permissions={[Permissions.APPOINTMENT_CREATE]}>
            <div className="space-y-4 rounded-xl border bg-card p-4">
              <h2 className="text-sm font-semibold">Publish weekday schedule (Mon–Fri)</h2>
              <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
                <div className="space-y-2">
                  <Label htmlFor="effective-from">Effective from</Label>
                  <Input
                    id="effective-from"
                    type="date"
                    value={effectiveFrom}
                    onChange={(event) => setEffectiveFrom(event.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="start-time">Start</Label>
                  <Input
                    id="start-time"
                    type="time"
                    value={startTime}
                    onChange={(event) => setStartTime(event.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="end-time">End</Label>
                  <Input
                    id="end-time"
                    type="time"
                    value={endTime}
                    onChange={(event) => setEndTime(event.target.value)}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="max-per-day">Max / day</Label>
                  <Input
                    id="max-per-day"
                    type="number"
                    min={1}
                    max={500}
                    value={maxPerDay}
                    onChange={(event) => setMaxPerDay(Number(event.target.value) || 1)}
                  />
                </div>
              </div>
              <Button
                type="button"
                disabled={createMutation.isPending}
                onClick={() => createMutation.mutate()}
              >
                {createMutation.isPending ? (
                  <Loader2Icon className="animate-spin" data-icon="inline-start" />
                ) : null}
                Create schedule
              </Button>
            </div>
          </Can>

          {schedulesQuery.isLoading ? (
            <div className="text-muted-foreground py-12 text-center text-sm">
              Loading schedules…
            </div>
          ) : schedulesQuery.isError ? (
            <EmptyState
              title="Unable to load schedules"
              description={getErrorMessage(schedulesQuery.error)}
            />
          ) : (schedulesQuery.data?.length ?? 0) === 0 ? (
            <EmptyState
              title="No schedules published"
              description="Create a weekday schedule before booking appointments for this doctor."
            />
          ) : (
            <div className="overflow-x-auto rounded-xl border bg-card">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Name</TableHead>
                    <TableHead>Effective</TableHead>
                    <TableHead>Max / day</TableHead>
                    <TableHead>Windows</TableHead>
                    <TableHead>Status</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {schedulesQuery.data?.map((schedule) => (
                    <TableRow key={schedule.id}>
                      <TableCell>{schedule.name ?? '—'}</TableCell>
                      <TableCell className="text-sm whitespace-nowrap">
                        {schedule.effectiveFrom}
                        {schedule.effectiveTo ? ` → ${schedule.effectiveTo}` : ' → open'}
                      </TableCell>
                      <TableCell>{schedule.maxAppointmentsPerDay}</TableCell>
                      <TableCell className="text-muted-foreground text-xs">
                        {schedule.windows.length} day window(s)
                      </TableCell>
                      <TableCell>
                        <StatusBadge status={schedule.status} />
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </>
      )}
    </div>
  );
}
