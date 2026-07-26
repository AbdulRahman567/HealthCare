'use client';

import type { UseFormReturn } from 'react-hook-form';

import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { EnumSelect, FieldError } from '@/features/patients/components/shared/enum-select';
import {
  BLOOD_GROUPS,
  BLOOD_GROUP_LABELS,
  GENDERS,
  MARITAL_STATUSES,
} from '@/features/patients/types/enums';
import type { PatientFormValues } from '@/features/patients/validation/patient-schema';

type PatientFormFieldsProps = {
  form: UseFormReturn<PatientFormValues>;
  idPrefix?: string;
  mrnReadOnly?: boolean;
};

export function PatientFormFields({
  form,
  idPrefix = 'patient',
  mrnReadOnly = false,
}: PatientFormFieldsProps) {
  const { register, watch, setValue, formState } = form;
  const errors = formState.errors;

  return (
    <div className="grid gap-6">
      <section className="grid gap-4 sm:grid-cols-2" aria-labelledby={`${idPrefix}-identity`}>
        <h2 id={`${idPrefix}-identity`} className="text-sm font-semibold sm:col-span-2">
          Identity
        </h2>
        <div className="space-y-2">
          <Label htmlFor={`${idPrefix}-mrn`}>Medical record number (MRN)</Label>
          <Input
            id={`${idPrefix}-mrn`}
            autoComplete="off"
            readOnly={mrnReadOnly}
            aria-invalid={Boolean(errors.mrn)}
            {...register('mrn')}
          />
          <FieldError message={errors.mrn?.message} />
        </div>
        <div className="space-y-2">
          <Label htmlFor={`${idPrefix}-dob`}>Date of birth</Label>
          <Input
            id={`${idPrefix}-dob`}
            type="date"
            aria-invalid={Boolean(errors.dateOfBirth)}
            {...register('dateOfBirth')}
          />
          <FieldError message={errors.dateOfBirth?.message} />
        </div>
        <div className="space-y-2">
          <Label htmlFor={`${idPrefix}-first`}>First name</Label>
          <Input
            id={`${idPrefix}-first`}
            autoComplete="given-name"
            aria-invalid={Boolean(errors.firstName)}
            {...register('firstName')}
          />
          <FieldError message={errors.firstName?.message} />
        </div>
        <div className="space-y-2">
          <Label htmlFor={`${idPrefix}-last`}>Last name</Label>
          <Input
            id={`${idPrefix}-last`}
            autoComplete="family-name"
            aria-invalid={Boolean(errors.lastName)}
            {...register('lastName')}
          />
          <FieldError message={errors.lastName?.message} />
        </div>
        <EnumSelect
          id={`${idPrefix}-gender`}
          label="Gender"
          value={watch('gender')}
          onValueChange={(value) =>
            setValue('gender', value as PatientFormValues['gender'], { shouldValidate: true })
          }
          options={GENDERS}
          error={errors.gender?.message}
        />
        <EnumSelect
          id={`${idPrefix}-blood`}
          label="Blood group"
          value={watch('bloodGroup') || ''}
          onValueChange={(value) =>
            setValue('bloodGroup', value as PatientFormValues['bloodGroup'], {
              shouldValidate: true,
            })
          }
          options={BLOOD_GROUPS}
          labels={BLOOD_GROUP_LABELS}
          optional
          error={errors.bloodGroup?.message}
        />
        <EnumSelect
          id={`${idPrefix}-marital`}
          label="Marital status"
          value={watch('maritalStatus') || ''}
          onValueChange={(value) =>
            setValue('maritalStatus', value as PatientFormValues['maritalStatus'], {
              shouldValidate: true,
            })
          }
          options={MARITAL_STATUSES}
          optional
          error={errors.maritalStatus?.message}
        />
        <div className="space-y-2">
          <Label htmlFor={`${idPrefix}-national-id`}>National ID / CNIC</Label>
          <Input
            id={`${idPrefix}-national-id`}
            autoComplete="off"
            aria-invalid={Boolean(errors.nationalId)}
            {...register('nationalId')}
          />
          <FieldError message={errors.nationalId?.message} />
        </div>
      </section>

      <section className="grid gap-4 sm:grid-cols-2" aria-labelledby={`${idPrefix}-contact`}>
        <h2 id={`${idPrefix}-contact`} className="text-sm font-semibold sm:col-span-2">
          Contact
        </h2>
        <div className="space-y-2">
          <Label htmlFor={`${idPrefix}-phone`}>Phone</Label>
          <Input
            id={`${idPrefix}-phone`}
            type="tel"
            autoComplete="tel"
            aria-invalid={Boolean(errors.phone)}
            {...register('phone')}
          />
          <FieldError message={errors.phone?.message} />
        </div>
        <div className="space-y-2">
          <Label htmlFor={`${idPrefix}-email`}>Email</Label>
          <Input
            id={`${idPrefix}-email`}
            type="email"
            autoComplete="email"
            aria-invalid={Boolean(errors.email)}
            {...register('email')}
          />
          <FieldError message={errors.email?.message} />
        </div>
        <div className="space-y-2 sm:col-span-2">
          <Label htmlFor={`${idPrefix}-address`}>Address</Label>
          <Textarea
            id={`${idPrefix}-address`}
            rows={3}
            aria-invalid={Boolean(errors.address)}
            {...register('address')}
          />
          <FieldError message={errors.address?.message} />
        </div>
      </section>

      <section className="grid gap-4 sm:grid-cols-2" aria-labelledby={`${idPrefix}-emergency`}>
        <h2 id={`${idPrefix}-emergency`} className="text-sm font-semibold sm:col-span-2">
          Emergency contact
        </h2>
        <p className="text-muted-foreground text-xs sm:col-span-2">
          If any emergency contact field is provided, name and phone are both required.
        </p>
        <div className="space-y-2">
          <Label htmlFor={`${idPrefix}-ec-name`}>Name</Label>
          <Input
            id={`${idPrefix}-ec-name`}
            aria-invalid={Boolean(errors.emergencyContactName)}
            {...register('emergencyContactName')}
          />
          <FieldError message={errors.emergencyContactName?.message} />
        </div>
        <div className="space-y-2">
          <Label htmlFor={`${idPrefix}-ec-phone`}>Phone</Label>
          <Input
            id={`${idPrefix}-ec-phone`}
            type="tel"
            aria-invalid={Boolean(errors.emergencyContactPhone)}
            {...register('emergencyContactPhone')}
          />
          <FieldError message={errors.emergencyContactPhone?.message} />
        </div>
        <div className="space-y-2 sm:col-span-2">
          <Label htmlFor={`${idPrefix}-ec-relation`}>Relation</Label>
          <Input
            id={`${idPrefix}-ec-relation`}
            aria-invalid={Boolean(errors.emergencyContactRelation)}
            {...register('emergencyContactRelation')}
          />
          <FieldError message={errors.emergencyContactRelation?.message} />
        </div>
      </section>
    </div>
  );
}
