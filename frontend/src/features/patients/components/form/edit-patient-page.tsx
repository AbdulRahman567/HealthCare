'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2Icon } from 'lucide-react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { AdminPageHeader } from '@/features/hospital-admin/components/shared/admin-page-header';
import { EmptyState } from '@/features/hospital-admin/components/shared/empty-state';
import { PatientFormFields } from '@/features/patients/components/form/patient-form-fields';
import { usePatientMutations, usePatientQuery } from '@/features/patients/hooks/use-patients';
import {
  formValuesToPatientPayload,
  patientDisplayName,
  patientToFormValues,
} from '@/features/patients/lib/patient-format';
import {
  emptyPatientForm,
  patientFormSchema,
  type PatientFormValues,
} from '@/features/patients/validation/patient-schema';
import { getErrorMessage } from '@/lib/api-error';

type EditPatientPageProps = {
  patientId: string;
};

export function EditPatientPage({ patientId }: EditPatientPageProps) {
  const router = useRouter();
  const patientQuery = usePatientQuery(patientId);
  const mutations = usePatientMutations();
  const form = useForm<PatientFormValues>({
    resolver: zodResolver(patientFormSchema),
    defaultValues: emptyPatientForm(),
  });

  useEffect(() => {
    if (patientQuery.data) {
      form.reset(patientToFormValues(patientQuery.data));
    }
  }, [patientQuery.data, form]);

  const onSubmit = form.handleSubmit(async (values) => {
    try {
      await mutations.update.mutateAsync({
        id: patientId,
        payload: formValuesToPatientPayload(values),
      });
      toast.success('Patient updated');
      router.push(`/app/patients/${patientId}`);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to update patient'));
    }
  });

  if (patientQuery.isError) {
    return (
      <EmptyState
        title="Unable to load patient"
        description={getErrorMessage(patientQuery.error)}
        action={
          <Button nativeButton={false} variant="outline" render={<Link href="/app/patients" />}>
            Back to list
          </Button>
        }
      />
    );
  }

  if (patientQuery.isLoading || !patientQuery.data) {
    return (
      <div className="text-muted-foreground px-6 py-16 text-center text-sm">Loading patient…</div>
    );
  }

  const patient = patientQuery.data;
  const isBusy = mutations.update.isPending;

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <AdminPageHeader
        title={`Edit ${patientDisplayName(patient)}`}
        description={`Update demographics for MRN ${patient.mrn}. Status changes use activate/deactivate on the chart.`}
        actions={
          <Button
            nativeButton={false}
            variant="outline"
            render={<Link href={`/app/patients/${patientId}`} />}
          >
            Back to chart
          </Button>
        }
      />

      <form
        onSubmit={onSubmit}
        noValidate
        className="space-y-6 rounded-xl border bg-card p-4 sm:p-6"
      >
        <PatientFormFields form={form} idPrefix="edit" mrnReadOnly />
        <div className="flex flex-col-reverse gap-2 border-t pt-4 sm:flex-row sm:justify-end">
          <Button
            nativeButton={false}
            variant="outline"
            render={<Link href={`/app/patients/${patientId}`} />}
          >
            Cancel
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
