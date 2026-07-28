'use client';

import { CheckIcon, Loader2Icon, PencilIcon, StethoscopeIcon, UserCheckIcon } from 'lucide-react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Can } from '@/features/authorization/components/can';
import { Permissions } from '@/features/authorization/constants/permissions';
import { CancelAppointmentDialog } from '@/features/appointments/components/shared/cancel-appointment-dialog';
import { AppointmentSubnav } from '@/features/appointments/components/shared/appointment-subnav';
import { useAppointmentLookups } from '@/features/appointments/hooks/use-appointment-lookups';
import {
  useAppointmentMutations,
  useAppointmentQuery,
} from '@/features/appointments/hooks/use-appointments';
import { useQueueMutations } from '@/features/appointments/hooks/use-queue';
import {
  formatAppointmentSlot,
  formatTimeLabel,
  isMutableAppointmentStatus,
} from '@/features/appointments/lib/appointment-format';
import { AdminPageHeader } from '@/features/hospital-admin/components/shared/admin-page-header';
import { EmptyState } from '@/features/hospital-admin/components/shared/empty-state';
import { StatusBadge } from '@/features/hospital-admin/components/shared/status-badge';
import { getErrorMessage } from '@/lib/api-error';
import { formatEnumLabel } from '@/lib/page-query';

type AppointmentDetailPageProps = {
  appointmentId: string;
};

export function AppointmentDetailPage({ appointmentId }: AppointmentDetailPageProps) {
  const router = useRouter();
  const appointmentQuery = useAppointmentQuery(appointmentId);
  const mutations = useAppointmentMutations();
  const queueMutations = useQueueMutations();
  const [cancelOpen, setCancelOpen] = useState(false);

  const appointment = appointmentQuery.data;
  const lookups = useAppointmentLookups(appointment ? [appointment] : []);

  if (appointmentQuery.isError) {
    return (
      <div className="mx-auto max-w-3xl space-y-6">
        <EmptyState
          title="Appointment not found"
          description={getErrorMessage(appointmentQuery.error)}
          action={
            <Button
              nativeButton={false}
              variant="outline"
              render={<Link href="/app/appointments/list" />}
            >
              Back to list
            </Button>
          }
        />
      </div>
    );
  }

  if (appointmentQuery.isLoading || !appointment) {
    return (
      <div className="text-muted-foreground mx-auto max-w-3xl py-16 text-center text-sm">
        Loading appointment…
      </div>
    );
  }

  const mutable = isMutableAppointmentStatus(appointment.status);

  const onConfirm = async () => {
    try {
      await mutations.confirm.mutateAsync(appointment.id);
      toast.success('Appointment confirmed');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to confirm appointment'));
    }
  };

  const onCheckIn = async () => {
    try {
      await queueMutations.checkIn.mutateAsync({ appointmentId: appointment.id });
      toast.success('Patient checked in to queue');
      router.push('/app/appointments/queue');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to check in'));
    }
  };

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <AdminPageHeader
        title={appointment.appointmentNumber}
        description={formatAppointmentSlot(
          appointment.appointmentDate,
          appointment.startTime,
          appointment.endTime,
        )}
        actions={
          <div className="flex flex-wrap gap-2">
            <Button
              nativeButton={false}
              variant="outline"
              render={<Link href="/app/appointments/list" />}
            >
              Back
            </Button>
            {mutable ? (
              <Can permissions={[Permissions.APPOINTMENT_UPDATE]}>
                <Button
                  nativeButton={false}
                  render={<Link href={`/app/appointments/${appointment.id}/edit`} />}
                >
                  <PencilIcon data-icon="inline-start" />
                  Edit
                </Button>
              </Can>
            ) : null}
            <Can permissions={[Permissions.VISIT_CREATE]}>
              <Button
                nativeButton={false}
                variant="secondary"
                render={
                  <Link
                    href={`/app/clinical/new?appointmentId=${appointment.id}&patientId=${appointment.patientId}&doctorId=${appointment.doctorId}&departmentId=${appointment.departmentId}`}
                  />
                }
              >
                <StethoscopeIcon data-icon="inline-start" />
                Start consultation
              </Button>
            </Can>
          </div>
        }
      />

      <AppointmentSubnav />

      <Card>
        <CardHeader className="flex flex-row items-center justify-between gap-2 space-y-0">
          <CardTitle className="text-base">Appointment details</CardTitle>
          <StatusBadge status={appointment.status} />
        </CardHeader>
        <CardContent className="grid gap-4 sm:grid-cols-2">
          <Detail label="Patient" value={lookups.patientNameById.get(appointment.patientId)} />
          <Detail label="Doctor" value={lookups.doctorNameById.get(appointment.doctorId)} />
          <Detail
            label="Department"
            value={lookups.departmentNameById.get(appointment.departmentId)}
          />
          <Detail label="Visit type" value={formatEnumLabel(appointment.visitType)} />
          <Detail label="Appointment type" value={formatEnumLabel(appointment.appointmentType)} />
          <Detail
            label="Duration"
            value={`${appointment.durationMinutes} min (${formatTimeLabel(appointment.startTime)}–${formatTimeLabel(appointment.endTime)})`}
          />
          <Detail label="Notes" value={appointment.notes || '—'} className="sm:col-span-2" />
          {appointment.cancellationReason ? (
            <Detail
              label="Cancellation reason"
              value={appointment.cancellationReason}
              className="sm:col-span-2"
            />
          ) : null}
        </CardContent>
      </Card>

      <Can permissions={[Permissions.APPOINTMENT_UPDATE]}>
        <div className="flex flex-wrap gap-2">
          {appointment.status === 'SCHEDULED' ? (
            <Button onClick={onConfirm} disabled={mutations.confirm.isPending}>
              {mutations.confirm.isPending ? (
                <Loader2Icon className="animate-spin" data-icon="inline-start" />
              ) : (
                <CheckIcon data-icon="inline-start" />
              )}
              Confirm
            </Button>
          ) : null}
          {mutable ? (
            <>
              <Button
                variant="outline"
                onClick={onCheckIn}
                disabled={queueMutations.checkIn.isPending}
              >
                {queueMutations.checkIn.isPending ? (
                  <Loader2Icon className="animate-spin" data-icon="inline-start" />
                ) : (
                  <UserCheckIcon data-icon="inline-start" />
                )}
                Check in to queue
              </Button>
              <Button variant="destructive" onClick={() => setCancelOpen(true)}>
                Cancel appointment
              </Button>
            </>
          ) : null}
        </div>
      </Can>

      <CancelAppointmentDialog
        appointmentId={appointment.id}
        appointmentNumber={appointment.appointmentNumber}
        open={cancelOpen}
        onOpenChange={setCancelOpen}
      />
    </div>
  );
}

function Detail({
  label,
  value,
  className,
}: {
  label: string;
  value?: string | null;
  className?: string;
}) {
  return (
    <div className={className}>
      <p className="text-muted-foreground text-xs font-medium tracking-wide uppercase">{label}</p>
      <p className="mt-1 text-sm font-medium text-pretty">{value || '—'}</p>
    </div>
  );
}
