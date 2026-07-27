'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2Icon } from 'lucide-react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { AppointmentFormFields } from '@/features/appointments/components/form/appointment-form-fields';
import { AppointmentSubnav } from '@/features/appointments/components/shared/appointment-subnav';
import { useAppointmentMutations } from '@/features/appointments/hooks/use-appointments';
import { toCreatePayload } from '@/features/appointments/lib/appointment-format';
import {
  emptyAppointmentForm,
  appointmentFormSchema,
  type AppointmentFormValues,
} from '@/features/appointments/validation/appointment-schema';
import { AdminPageHeader } from '@/features/hospital-admin/components/shared/admin-page-header';
import { getErrorMessage } from '@/lib/api-error';

export function BookAppointmentPage() {
  const router = useRouter();
  const mutations = useAppointmentMutations();
  const form = useForm<AppointmentFormValues>({
    resolver: zodResolver(appointmentFormSchema),
    defaultValues: emptyAppointmentForm(),
  });

  const onSubmit = form.handleSubmit(async (values) => {
    try {
      const appointment = await mutations.create.mutateAsync(toCreatePayload(values));
      toast.success('Appointment booked');
      router.push(`/app/appointments/${appointment.id}`);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to book appointment'));
    }
  });

  const isBusy = mutations.create.isPending;

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <AdminPageHeader
        title="Book appointment"
        description="Schedule a patient with an available doctor. The server validates availability, capacity, and conflicts."
        actions={
          <Button
            nativeButton={false}
            variant="outline"
            render={<Link href="/app/appointments/list" />}
          >
            Back to list
          </Button>
        }
      />

      <AppointmentSubnav />

      <form
        onSubmit={onSubmit}
        noValidate
        className="space-y-6 rounded-xl border bg-card p-4 sm:p-6"
      >
        <AppointmentFormFields form={form} idPrefix="book" />
        <div className="flex flex-col-reverse gap-2 border-t pt-4 sm:flex-row sm:justify-end">
          <Button
            nativeButton={false}
            variant="outline"
            render={<Link href="/app/appointments/list" />}
          >
            Cancel
          </Button>
          <Button type="submit" disabled={isBusy}>
            {isBusy ? <Loader2Icon className="animate-spin" data-icon="inline-start" /> : null}
            Book appointment
          </Button>
        </div>
      </form>
    </div>
  );
}
