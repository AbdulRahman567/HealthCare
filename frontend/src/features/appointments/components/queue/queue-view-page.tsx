'use client';

import { Loader2Icon } from 'lucide-react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useEffect, useMemo, useState } from 'react';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { Card, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
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
import { AppointmentSubnav } from '@/features/appointments/components/shared/appointment-subnav';
import { useAppointmentLookups } from '@/features/appointments/hooks/use-appointment-lookups';
import { useDoctorDayQueueQuery, useQueueMutations } from '@/features/appointments/hooks/use-queue';
import type { QueueAction } from '@/features/appointments/types/queue';
import type { QueueEntryStatus } from '@/features/appointments/types/enums';
import {
  selectAppointmentsQueueUi,
  setQueueDate,
  setQueueDoctorId,
} from '@/features/appointments/store/appointments-ui-slice';
import { useUsersQuery } from '@/features/hospital-admin/hooks/use-users';
import { AdminPageHeader } from '@/features/hospital-admin/components/shared/admin-page-header';
import { EmptyState } from '@/features/hospital-admin/components/shared/empty-state';
import { StatusBadge } from '@/features/hospital-admin/components/shared/status-badge';
import { getErrorMessage } from '@/lib/api-error';
import { formatPersonName } from '@/lib/page-query';
import { useAppDispatch, useAppSelector } from '@/store/hooks';

const NEXT_ACTIONS: Partial<Record<QueueEntryStatus, { action: QueueAction; label: string }[]>> = {
  CHECKED_IN: [
    { action: 'waiting', label: 'Mark waiting' },
    { action: 'missed', label: 'Missed' },
    { action: 'cancel', label: 'Cancel' },
  ],
  WAITING: [
    { action: 'start-consultation', label: 'Start consult' },
    { action: 'missed', label: 'Missed' },
    { action: 'cancel', label: 'Cancel' },
  ],
  IN_CONSULTATION: [{ action: 'cancel', label: 'Cancel' }],
};

const DESTRUCTIVE_ACTIONS: QueueAction[] = ['missed', 'cancel'];

export function QueueViewPage() {
  const router = useRouter();
  const dispatch = useAppDispatch();
  const ui = useAppSelector(selectAppointmentsQueueUi);
  const lookups = useAppointmentLookups([]);
  const usersQuery = useUsersQuery({
    page: 0,
    size: 100,
    status: 'ACTIVE',
    roleType: 'DOCTOR',
    sort: ['lastName,asc', 'firstName,asc'],
  });
  const mutations = useQueueMutations();
  const [pendingAction, setPendingAction] = useState<{
    entryId: string;
    action: QueueAction;
    label: string;
  } | null>(null);

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
    if (!ui.doctorId && doctorOptions.length > 0) {
      dispatch(setQueueDoctorId(doctorOptions[0].id));
    }
  }, [dispatch, doctorOptions, ui.doctorId]);

  const queueQuery = useDoctorDayQueueQuery(
    ui.doctorId,
    ui.date || undefined,
    Boolean(ui.doctorId),
  );
  const entries = queueQuery.data?.entries ?? [];

  const runAction = async (entryId: string, action: QueueAction) => {
    try {
      const result = await mutations.updateStatus.mutateAsync({ entryId, action });
      if (action === 'start-consultation' && result.consultationId) {
        toast.success('Consultation started — opening chart');
        setPendingAction(null);
        router.push(`/app/clinical/${result.consultationId}`);
        return;
      }
      toast.success('Queue updated');
      setPendingAction(null);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to update queue'));
    }
  };

  const onAction = (entryId: string, action: QueueAction, label: string) => {
    if (DESTRUCTIVE_ACTIONS.includes(action)) {
      setPendingAction({ entryId, action, label });
      return;
    }
    void runAction(entryId, action);
  };

  return (
    <div className="mx-auto max-w-6xl space-y-6">
      <AdminPageHeader
        title="Doctor queue"
        description="Manage the daily OPD queue — check-in from appointment detail, then advance patients through consultation."
      />

      <AppointmentSubnav />

      <div className="grid gap-3 rounded-xl border bg-card p-4 sm:grid-cols-2">
        <div className="space-y-2">
          <Label>Doctor</Label>
          <Select
            value={ui.doctorId || undefined}
            onValueChange={(value) => dispatch(setQueueDoctorId(value ?? ''))}
          >
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
        <div className="space-y-2">
          <Label htmlFor="queue-date">Date</Label>
          <Input
            id="queue-date"
            type="date"
            value={ui.date}
            onChange={(event) => dispatch(setQueueDate(event.target.value))}
          />
        </div>
      </div>

      {!ui.doctorId ? (
        <EmptyState
          title="Select a doctor"
          description="Choose a doctor to load today’s queue board."
        />
      ) : queueQuery.isError ? (
        <EmptyState title="Unable to load queue" description={getErrorMessage(queueQuery.error)} />
      ) : queueQuery.isLoading || !queueQuery.data ? (
        <div className="text-muted-foreground py-16 text-center text-sm">Loading queue…</div>
      ) : (
        <>
          <div className="grid gap-4 sm:grid-cols-3">
            <Card>
              <CardHeader className="pb-2">
                <CardDescription>Last number</CardDescription>
                <CardTitle className="text-3xl tabular-nums">
                  {queueQuery.data.lastQueueNumber}
                </CardTitle>
              </CardHeader>
            </Card>
            <Card>
              <CardHeader className="pb-2">
                <CardDescription>Waiting</CardDescription>
                <CardTitle className="text-3xl tabular-nums">
                  {queueQuery.data.waitingCount}
                </CardTitle>
              </CardHeader>
            </Card>
            <Card>
              <CardHeader className="pb-2">
                <CardDescription>In consultation</CardDescription>
                <CardTitle className="text-3xl tabular-nums">
                  {queueQuery.data.inConsultationCount}
                </CardTitle>
              </CardHeader>
            </Card>
          </div>

          {entries.length === 0 ? (
            <EmptyState
              title="Queue is empty"
              description="Check a patient in from an appointment detail page."
              action={
                <Button
                  nativeButton={false}
                  variant="outline"
                  render={<Link href="/app/appointments/list" />}
                >
                  Browse appointments
                </Button>
              }
            />
          ) : (
            <div className="overflow-x-auto rounded-xl border bg-card">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>#</TableHead>
                    <TableHead>Patient</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Checked in</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {entries.map((entry) => {
                    const actions = NEXT_ACTIONS[entry.status] ?? [];
                    return (
                      <TableRow key={entry.id}>
                        <TableCell className="font-mono text-sm font-semibold">
                          {entry.queueNumber}
                        </TableCell>
                        <TableCell>
                          <Link
                            href={`/app/appointments/${entry.appointmentId}`}
                            className="font-medium hover:underline"
                          >
                            {entry.patientName ?? 'Patient'}
                          </Link>
                        </TableCell>
                        <TableCell>
                          <StatusBadge status={entry.status} />
                        </TableCell>
                        <TableCell className="text-muted-foreground text-xs whitespace-nowrap">
                          {new Date(entry.checkedInAt).toLocaleTimeString([], {
                            hour: '2-digit',
                            minute: '2-digit',
                          })}
                        </TableCell>
                        <TableCell className="text-right">
                          <div className="flex flex-wrap justify-end gap-1">
                            {entry.consultationId ? (
                              <Can permissions={[Permissions.VISIT_READ]}>
                                <Button
                                  nativeButton={false}
                                  size="sm"
                                  variant="outline"
                                  render={<Link href={`/app/clinical/${entry.consultationId}`} />}
                                >
                                  Open chart
                                </Button>
                              </Can>
                            ) : null}
                            <Can permissions={[Permissions.APPOINTMENT_UPDATE]}>
                              {actions.map((item) => (
                                <Button
                                  key={item.action}
                                  size="sm"
                                  variant={
                                    item.action === 'cancel' || item.action === 'missed'
                                      ? 'outline'
                                      : 'secondary'
                                  }
                                  disabled={mutations.updateStatus.isPending}
                                  onClick={() => onAction(entry.id, item.action, item.label)}
                                >
                                  {mutations.updateStatus.isPending ? (
                                    <Loader2Icon
                                      className="animate-spin"
                                      data-icon="inline-start"
                                    />
                                  ) : null}
                                  {item.label}
                                </Button>
                              ))}
                            </Can>
                          </div>
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            </div>
          )}
        </>
      )}

      <Dialog
        open={Boolean(pendingAction)}
        onOpenChange={(open) => {
          if (!open) {
            setPendingAction(null);
          }
        }}
      >
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Confirm queue action</DialogTitle>
            <DialogDescription>
              {pendingAction?.action === 'missed'
                ? 'Mark this patient as missed? The appointment will be marked MISSED.'
                : 'Remove this patient from the active queue? They can check in again later.'}
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => setPendingAction(null)}>
              Keep on queue
            </Button>
            <Button
              type="button"
              variant="destructive"
              disabled={mutations.updateStatus.isPending || !pendingAction}
              onClick={() => {
                if (pendingAction) {
                  void runAction(pendingAction.entryId, pendingAction.action);
                }
              }}
            >
              {mutations.updateStatus.isPending ? (
                <Loader2Icon className="animate-spin" data-icon="inline-start" />
              ) : null}
              {pendingAction?.label ?? 'Confirm'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
