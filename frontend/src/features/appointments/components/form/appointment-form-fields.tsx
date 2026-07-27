'use client';

import type { UseFormReturn } from 'react-hook-form';
import { useEffect, useMemo } from 'react';

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
import { minutesBetween } from '@/features/appointments/lib/appointment-format';
import type { AppointmentFormValues } from '@/features/appointments/validation/appointment-schema';
import {
  APPOINTMENT_TYPES,
  VISIT_TYPES,
} from '@/features/appointments/types/enums';
import { useDepartmentsQuery } from '@/features/hospital-admin/hooks/use-departments';
import { useStaffQuery } from '@/features/hospital-admin/hooks/use-staff';
import { useUsersQuery } from '@/features/hospital-admin/hooks/use-users';
import { EnumSelect, FieldError } from '@/features/patients/components/shared/enum-select';
import { usePatientsQuery } from '@/features/patients/hooks/use-patients';
import { patientDisplayName } from '@/features/patients/lib/patient-format';
import { formatPersonName } from '@/lib/page-query';

type AppointmentFormFieldsProps = {
  form: UseFormReturn<AppointmentFormValues>;
  idPrefix: string;
  lockPatient?: boolean;
};

export function AppointmentFormFields({
  form,
  idPrefix,
  lockPatient = false,
}: AppointmentFormFieldsProps) {
  const {
    register,
    watch,
    setValue,
    formState: { errors },
  } = form;

  const doctorId = watch('doctorId');
  const startTime = watch('startTime');
  const endTime = watch('endTime');

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
      const specialization =
        'specialization' in doctor && typeof doctor.specialization === 'string'
          ? doctor.specialization
          : null;
      map.set(
        doctor.id,
        name
          ? `${name}${specialization ? ` · ${specialization}` : ''} (${doctor.employeeCode})`
          : specialization
            ? `${doctor.employeeCode} · ${specialization}`
            : doctor.employeeCode,
      );
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

  const duration =
    startTime && endTime && minutesBetween(startTime, endTime) > 0
      ? minutesBetween(startTime, endTime)
      : null;

  return (
    <div className="grid gap-4 sm:grid-cols-2">
      <div className="space-y-2 sm:col-span-2">
        <Label htmlFor={`${idPrefix}-patient`}>Patient</Label>
        <Select
          value={watch('patientId') || undefined}
          onValueChange={(value) => setValue('patientId', value ?? '', { shouldValidate: true })}
          disabled={lockPatient}
        >
          <SelectTrigger id={`${idPrefix}-patient`} aria-invalid={Boolean(errors.patientId)}>
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

      <div className="space-y-2 sm:col-span-2">
        <Label htmlFor={`${idPrefix}-doctor`}>Doctor</Label>
        <Select
          value={watch('doctorId') || undefined}
          onValueChange={(value) => setValue('doctorId', value ?? '', { shouldValidate: true })}
        >
          <SelectTrigger id={`${idPrefix}-doctor`} aria-invalid={Boolean(errors.doctorId)}>
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

      <div className="space-y-2 sm:col-span-2">
        <Label htmlFor={`${idPrefix}-department`}>Department</Label>
        <Select
          value={watch('departmentId') || undefined}
          onValueChange={(value) =>
            setValue('departmentId', value ?? '', { shouldValidate: true })
          }
        >
          <SelectTrigger
            id={`${idPrefix}-department`}
            aria-invalid={Boolean(errors.departmentId)}
          >
            <SelectValue placeholder="Select department" />
          </SelectTrigger>
          <SelectContent>
            {(departmentsQuery.data?.content ?? []).map((department) => (
              <SelectItem key={department.id} value={department.id}>
                {department.name} ({department.code})
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        <FieldError message={errors.departmentId?.message} />
      </div>

      <div className="space-y-2">
        <Label htmlFor={`${idPrefix}-date`}>Date</Label>
        <Input
          id={`${idPrefix}-date`}
          type="date"
          aria-invalid={Boolean(errors.appointmentDate)}
          {...register('appointmentDate')}
        />
        <FieldError message={errors.appointmentDate?.message} />
      </div>

      <div className="grid grid-cols-2 gap-3">
        <div className="space-y-2">
          <Label htmlFor={`${idPrefix}-start`}>Start</Label>
          <Input
            id={`${idPrefix}-start`}
            type="time"
            aria-invalid={Boolean(errors.startTime)}
            {...register('startTime')}
          />
          <FieldError message={errors.startTime?.message} />
        </div>
        <div className="space-y-2">
          <Label htmlFor={`${idPrefix}-end`}>End</Label>
          <Input
            id={`${idPrefix}-end`}
            type="time"
            aria-invalid={Boolean(errors.endTime)}
            {...register('endTime')}
          />
          <FieldError message={errors.endTime?.message} />
        </div>
      </div>

      <div className="text-muted-foreground sm:col-span-2 text-sm">
        Duration:{' '}
        <span className="text-foreground font-medium">
          {duration !== null ? `${duration} minutes` : '—'}
        </span>
      </div>

      <EnumSelect
        id={`${idPrefix}-type`}
        label="Appointment type"
        value={watch('appointmentType')}
        onValueChange={(value) =>
          setValue('appointmentType', value as AppointmentFormValues['appointmentType'], {
            shouldValidate: true,
          })
        }
        options={APPOINTMENT_TYPES}
        error={errors.appointmentType?.message}
      />

      <EnumSelect
        id={`${idPrefix}-visit`}
        label="Visit type"
        value={watch('visitType')}
        onValueChange={(value) =>
          setValue('visitType', value as AppointmentFormValues['visitType'], {
            shouldValidate: true,
          })
        }
        options={VISIT_TYPES}
        error={errors.visitType?.message}
      />

      <div className="space-y-2 sm:col-span-2">
        <Label htmlFor={`${idPrefix}-notes`}>Notes</Label>
        <Textarea id={`${idPrefix}-notes`} rows={3} {...register('notes')} />
        <FieldError message={errors.notes?.message} />
      </div>
    </div>
  );
}
