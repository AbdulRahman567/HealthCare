'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2Icon } from 'lucide-react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useEffect, useMemo } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { Can } from '@/features/authorization/components/can';
import { Permissions } from '@/features/authorization/constants/permissions';
import { ClinicalSubnav } from '@/features/clinical/components/shared/clinical-subnav';
import { useConsultationMutations } from '@/features/clinical/hooks/use-consultations';
import { toCreateConsultationPayload } from '@/features/clinical/lib/clinical-format';
import {
  emptyStartConsultationForm,
  startConsultationSchema,
  type StartConsultationFormValues,
} from '@/features/clinical/validation/clinical-schemas';
import { AdminPageHeader } from '@/features/hospital-admin/components/shared/admin-page-header';
import { useDepartmentsQuery } from '@/features/hospital-admin/hooks/use-departments';
import { useStaffQuery } from '@/features/hospital-admin/hooks/use-staff';
import { useUsersQuery } from '@/features/hospital-admin/hooks/use-users';
import { FieldError } from '@/features/patients/components/shared/enum-select';
import { usePatientsQuery } from '@/features/patients/hooks/use-patients';
import { patientDisplayName } from '@/features/patients/lib/patient-format';
import { getErrorMessage } from '@/lib/api-error';
import { formatPersonName } from '@/lib/page-query';

export function StartConsultationPage({
  initialAppointmentId,
  initialPatientId,
  initialDoctorId,
  initialDepartmentId,
}: {
  initialAppointmentId?: string;
  initialPatientId?: string;
  initialDoctorId?: string;
  initialDepartmentId?: string;
} = {}) {
  const router = useRouter();
  const mutations = useConsultationMutations();
  const form = useForm<StartConsultationFormValues>({
    resolver: zodResolver(startConsultationSchema),
    defaultValues: {
      ...emptyStartConsultationForm(),
      appointmentId: initialAppointmentId ?? '',
      patientId: initialPatientId ?? '',
      doctorId: initialDoctorId ?? '',
      departmentId: initialDepartmentId ?? '',
    },
  });

  const {
    register,
    watch,
    setValue,
    formState: { errors },
  } = form;

  const doctorId = watch('doctorId');

  const patientsQuery = usePatientsQuery({
    page: 0,
    size: 50,
    status: 'ACTIVE',
    sort: ['lastName,asc', 'firstName,asc'],
  });
  const doctorsQuery = useStaffQuery('DOCTOR', {
    page: 0,
    size: 100,
    employmentStatus: 'ACTIVE',
    sort: 'employeeCode,asc',
  });
  const usersQuery = useUsersQuery({
    page: 0,
    size: 100,
    status: 'ACTIVE',
    roleType: 'DOCTOR',
    sort: ['lastName,asc', 'firstName,asc'],
  });
  const departmentsQuery = useDepartmentsQuery({
    page: 0,
    size: 100,
    status: 'ACTIVE',
    sort: 'name,asc',
  });

  const doctorLabelById = useMemo(() => {
    const userNameById = new Map<string, string>();
    for (const user of usersQuery.data?.content ?? []) {
      userNameById.set(user.id, formatPersonName(user.firstName, user.lastName));
    }
    const map = new Map<string, string>();
    for (const doctor of doctorsQuery.data?.content ?? []) {
      const name = userNameById.get(doctor.userId);
      map.set(doctor.id, name ? `${name} (${doctor.employeeCode})` : doctor.employeeCode);
    }
    return map;
  }, [doctorsQuery.data, usersQuery.data]);

  useEffect(() => {
    if (!doctorId) {
      return;
    }
    const doctor = doctorsQuery.data?.content.find((row) => row.id === doctorId);
    if (doctor?.departmentId) {
      setValue('departmentId', doctor.departmentId, { shouldValidate: true });
    }
  }, [doctorId, doctorsQuery.data, setValue]);

  const onSubmit = form.handleSubmit(async (values) => {
    try {
      const consultation = await mutations.create.mutateAsync(toCreateConsultationPayload(values));
      toast.success(
        values.startImmediately ? 'Consultation started' : 'Consultation draft created',
      );
      router.push(`/app/clinical/${consultation.id}`);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to start consultation'));
    }
  });

  const isBusy = mutations.create.isPending;

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <AdminPageHeader
        title="Start consultation"
        description="Open a clinical encounter for a patient. You can start immediately or keep it as a draft."
        actions={
          <Button nativeButton={false} variant="outline" render={<Link href="/app/clinical" />}>
            Back to list
          </Button>
        }
      />

      <ClinicalSubnav />

      <Can
        permissions={[Permissions.VISIT_CREATE]}
        fallback={
          <p className="text-muted-foreground text-sm">
            You need visit create permission to start a consultation.
          </p>
        }
      >
        <form
          onSubmit={onSubmit}
          noValidate
          className="space-y-6 rounded-xl border bg-card p-4 sm:p-6"
        >
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="space-y-2 sm:col-span-2">
              <Label htmlFor="start-patient">Patient</Label>
              <Select
                value={watch('patientId') || undefined}
                onValueChange={(value) =>
                  setValue('patientId', value ?? '', { shouldValidate: true })
                }
              >
                <SelectTrigger id="start-patient" className="w-full">
                  <SelectValue placeholder="Select patient" />
                </SelectTrigger>
                <SelectContent>
                  {(patientsQuery.data?.content ?? []).map((patient) => (
                    <SelectItem key={patient.id} value={patient.id}>
                      {patientDisplayName(patient)} · {patient.mrn}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <FieldError message={errors.patientId?.message} />
            </div>

            <div className="space-y-2">
              <Label htmlFor="start-doctor">Doctor</Label>
              <Select
                value={watch('doctorId') || undefined}
                onValueChange={(value) =>
                  setValue('doctorId', value ?? '', { shouldValidate: true })
                }
              >
                <SelectTrigger id="start-doctor" className="w-full">
                  <SelectValue placeholder="Select doctor" />
                </SelectTrigger>
                <SelectContent>
                  {(doctorsQuery.data?.content ?? []).map((doctor) => (
                    <SelectItem key={doctor.id} value={doctor.id}>
                      {doctorLabelById.get(doctor.id) ?? doctor.employeeCode}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <FieldError message={errors.doctorId?.message} />
            </div>

            <div className="space-y-2">
              <Label htmlFor="start-department">Department</Label>
              <Select
                value={watch('departmentId') || undefined}
                onValueChange={(value) =>
                  setValue('departmentId', value ?? '', { shouldValidate: true })
                }
              >
                <SelectTrigger id="start-department" className="w-full">
                  <SelectValue placeholder="Select department" />
                </SelectTrigger>
                <SelectContent>
                  {(departmentsQuery.data?.content ?? []).map((department) => (
                    <SelectItem key={department.id} value={department.id}>
                      {department.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <FieldError message={errors.departmentId?.message} />
            </div>

            <div className="space-y-2 sm:col-span-2">
              <Label htmlFor="start-appointment">Appointment ID (optional)</Label>
              <Input
                id="start-appointment"
                placeholder="Link an existing appointment UUID"
                readOnly={Boolean(initialAppointmentId)}
                {...register('appointmentId')}
              />
              {initialAppointmentId ? (
                <p className="text-muted-foreground text-xs">
                  Prefilled from appointment detail.
                </p>
              ) : null}
              <FieldError message={errors.appointmentId?.message} />
            </div>

            <div className="space-y-2 sm:col-span-2">
              <Label htmlFor="start-complaint">Chief complaint</Label>
              <Textarea
                id="start-complaint"
                rows={3}
                placeholder="Primary reason for visit"
                {...register('chiefComplaint')}
              />
              <FieldError message={errors.chiefComplaint?.message} />
            </div>

            <label className="flex items-center gap-2 sm:col-span-2">
              <Checkbox
                checked={watch('startImmediately')}
                onCheckedChange={(checked) =>
                  setValue('startImmediately', checked === true, { shouldValidate: true })
                }
              />
              <span className="text-sm">Start immediately (IN_PROGRESS)</span>
            </label>
          </div>

          <div className="flex flex-col-reverse gap-2 border-t pt-4 sm:flex-row sm:justify-end">
            <Button
              nativeButton={false}
              variant="outline"
              render={<Link href="/app/clinical" />}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={isBusy}>
              {isBusy ? <Loader2Icon className="animate-spin" data-icon="inline-start" /> : null}
              Start consultation
            </Button>
          </div>
        </form>
      </Can>
    </div>
  );
}
