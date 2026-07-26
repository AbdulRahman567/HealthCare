'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2Icon } from 'lucide-react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { AdminPageHeader } from '@/features/hospital-admin/components/shared/admin-page-header';
import { PatientFormFields } from '@/features/patients/components/form/patient-form-fields';
import { usePatientMutations } from '@/features/patients/hooks/use-patients';
import { formValuesToPatientPayload } from '@/features/patients/lib/patient-format';
import {
  emptyPatientForm,
  patientFormSchema,
  type PatientFormValues,
} from '@/features/patients/validation/patient-schema';
import { getErrorMessage } from '@/lib/api-error';

export function RegisterPatientPage() {
  const router = useRouter();
  const mutations = usePatientMutations();
  const form = useForm<PatientFormValues>({
    resolver: zodResolver(patientFormSchema),
    defaultValues: emptyPatientForm(),
  });

  const onSubmit = form.handleSubmit(async (values) => {
    try {
      const patient = await mutations.register.mutateAsync(formValuesToPatientPayload(values));
      toast.success('Patient registered');
      router.push(`/app/patients/${patient.id}`);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to register patient'));
    }
  });

  const isBusy = mutations.register.isPending;

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <AdminPageHeader
        title="Register patient"
        description="Create a new patient chart with demographics and emergency contact details."
        actions={
          <Button
            nativeButton={false}
            variant="outline"
            render={<Link href="/app/patients" />}
          >
            Back to list
          </Button>
        }
      />

      <form
        onSubmit={onSubmit}
        noValidate
        className="space-y-6 rounded-xl border bg-card p-4 sm:p-6"
      >
        <PatientFormFields form={form} idPrefix="register" />
        <div className="flex flex-col-reverse gap-2 border-t pt-4 sm:flex-row sm:justify-end">
          <Button
            nativeButton={false}
            variant="outline"
            render={<Link href="/app/patients" />}
          >
            Cancel
          </Button>
          <Button type="submit" disabled={isBusy}>
            {isBusy ? <Loader2Icon className="animate-spin" data-icon="inline-start" /> : null}
            Register patient
          </Button>
        </div>
      </form>
    </div>
  );
}
