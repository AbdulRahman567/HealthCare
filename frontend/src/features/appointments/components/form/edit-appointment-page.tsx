'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2Icon } from 'lucide-react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { AppointmentFormFields } from '@/features/appointments/components/form/appointment-form-fields';
import { AppointmentSubnav } from '@/features/appointments/components/shared/appointment-subnav';
import {
  useAppointmentMutations,
  useAppointmentQuery,
} from '@/features/appointments/hooks/use-appointments';
import {
  appointmentToFormValues,
  isMutableAppointmentStatus,
  toUpdatePayload,
} from '@/features/appointments/lib/appointment-format';
import {
  appointmentFormSchema,
  emptyAppointmentForm,
  type AppointmentFormValues,
} from '@/features/appointments/validation/appointment-schema';
import { AdminPageHeader } from '@/features/hospital-admin/components/shared/admin-page-header';
import { EmptyState } from '@/features/hospital-admin/components/shared/empty-state';
import { getErrorMessage } from '@/lib/api-error';

type EditAppointmentPageProps = {
  appointmentId: string;
};

export function EditAppointmentPage({ appointmentId }: EditAppointmentPageProps) {
  const router = useRouter();
  const appointmentQuery = useAppointmentQuery(appointmentId);
  const mutations = useAppointmentMutations();
  const form = useForm<AppointmentFormValues>({
    resolver: zodResolver(appointmentFormSchema),
    defaultValues: emptyAppointmentForm(),
  });

  useEffect(() => {
    if (appointmentQuery.data) {
      form.reset(appointmentToFormValues(appointmentQuery.data));
    }
  }, [appointmentQuery.data, form]);

  const onSubmit = form.handleSubmit(async (values) => {
    try {
      await mutations.update.mutateAsync({
        id: appointmentId,
        payload: toUpdatePayload(values),
      });
      toast.success('Appointment updated');
      router.push(`/app/appointments/${appointmentId}`);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to update appointment'));
    }
  });

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

  if (appointmentQuery.isLoading || !appointmentQuery.data) {
    return (
      <div className="text-muted-foreground mx-auto max-w-3xl py-16 text-center text-sm">
        Loading appointment…
      </div>
    );
  }

  if (!isMutableAppointmentStatus(appointmentQuery.data.status)) {
    return (
      <div className="mx-auto max-w-3xl space-y-6">
        <EmptyState
          title="Appointment cannot be edited"
          description={`Status ${appointmentQuery.data.status} is terminal for scheduling changes.`}
          action={
            <Button
              nativeButton={false}
              variant="outline"
              render={<Link href={`/app/appointments/${appointmentId}`} />}
            >
              View appointment
            </Button>
          }
        />
      </div>
    );
  }

  const isBusy = mutations.update.isPending;

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <AdminPageHeader
        title="Edit appointment"
        description={`${appointmentQuery.data.appointmentNumber} — update slot, doctor, type, or notes.`}
        actions={
          <Button
            nativeButton={false}
            variant="outline"
            render={<Link href={`/app/appointments/${appointmentId}`} />}
          >
            Cancel
          </Button>
        }
      />

      <AppointmentSubnav />

      <form
        onSubmit={onSubmit}
        noValidate
        className="space-y-6 rounded-xl border bg-card p-4 sm:p-6"
      >
        <AppointmentFormFields form={form} idPrefix="edit" lockPatient />
        <div className="flex flex-col-reverse gap-2 border-t pt-4 sm:flex-row sm:justify-end">
          <Button
            nativeButton={false}
            variant="outline"
            render={<Link href={`/app/appointments/${appointmentId}`} />}
          >
            Discard
          </Button>
          <Button type="submit" disabled={isBusy}>
            {isBusy ? <Loader2Icon className="animate-spin" data-icon="inline-start" /> : null}
            Save changes
          </Button>
        </div>
      </form>
    </div>
  );
}
