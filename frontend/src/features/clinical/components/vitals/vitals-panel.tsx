'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2Icon, Trash2Icon } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Textarea } from '@/components/ui/textarea';
import { Can } from '@/features/authorization/components/can';
import { Permissions } from '@/features/authorization/constants/permissions';
import { DraftSaveIndicator } from '@/features/clinical/components/shared/draft-save-indicator';
import { useAutoSaveDraft } from '@/features/clinical/hooks/use-auto-save-draft';
import {
  useConsultationVitalsQuery,
  useVitalsMutations,
} from '@/features/clinical/hooks/use-vitals';
import { formatBp, formatInstant, toVitalsPayload } from '@/features/clinical/lib/clinical-format';
import {
  emptyVitalsForm,
  vitalsFormSchema,
  type VitalsFormValues,
} from '@/features/clinical/validation/clinical-schemas';
import { EmptyState } from '@/features/hospital-admin/components/shared/empty-state';
import { FieldError } from '@/features/patients/components/shared/enum-select';
import { getErrorMessage } from '@/lib/api-error';

type VitalsPanelProps = {
  consultationId: string;
  patientId: string;
  editable: boolean;
};

export function VitalsPanel({ consultationId, patientId, editable }: VitalsPanelProps) {
  const vitalsQuery = useConsultationVitalsQuery(consultationId);
  const mutations = useVitalsMutations(consultationId, patientId);
  const form = useForm<VitalsFormValues>({
    resolver: zodResolver(vitalsFormSchema),
    defaultValues: emptyVitalsForm(),
  });

  const {
    register,
    handleSubmit,
    reset,
    watch,
    formState: { errors },
  } = form;

  const { status, lastSavedAt, clearSavedDraft } = useAutoSaveDraft({
    consultationId,
    tab: 'vitals',
    watch,
    enabled: editable,
    onRestore: (values) => reset(values),
  });

  const onSubmit = handleSubmit(async (values) => {
    try {
      await mutations.record.mutateAsync(toVitalsPayload(values));
      clearSavedDraft();
      reset(emptyVitalsForm());
      toast.success('Vital signs recorded');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to record vitals'));
    }
  });

  const onDelete = async (id: string) => {
    try {
      await mutations.remove.mutateAsync(id);
      toast.success('Vital signs entry removed');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to delete vitals'));
    }
  };

  const rows = vitalsQuery.data ?? [];
  const busy = mutations.record.isPending;

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-base font-semibold">Vital signs</h2>
        <p className="text-muted-foreground text-xs">
          Append measurements for this encounter. BMI is calculated on the server.
        </p>
      </div>

      <Can permissions={[Permissions.VISIT_UPDATE]}>
        {editable ? (
          <form
            onSubmit={onSubmit}
            noValidate
            className="space-y-4 rounded-xl border bg-card p-4"
          >
            <div className="flex items-center justify-between gap-2">
              <h3 className="text-sm font-medium">Record measurement</h3>
              <DraftSaveIndicator status={status} lastSavedAt={lastSavedAt} />
            </div>
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
              <NumberField
                id="temp"
                label="Temp (°C)"
                error={errors.temperatureCelsius?.message}
                {...register('temperatureCelsius')}
              />
              <NumberField
                id="hr"
                label="Heart rate"
                error={errors.heartRateBpm?.message}
                {...register('heartRateBpm')}
              />
              <NumberField
                id="sys"
                label="Systolic BP"
                error={errors.systolicBp?.message}
                {...register('systolicBp')}
              />
              <NumberField
                id="dia"
                label="Diastolic BP"
                error={errors.diastolicBp?.message}
                {...register('diastolicBp')}
              />
              <NumberField
                id="rr"
                label="Resp. rate"
                error={errors.respiratoryRate?.message}
                {...register('respiratoryRate')}
              />
              <NumberField
                id="spo2"
                label="SpO₂ %"
                error={errors.oxygenSaturationPercent?.message}
                {...register('oxygenSaturationPercent')}
              />
              <NumberField
                id="height"
                label="Height (cm)"
                error={errors.heightCm?.message}
                {...register('heightCm')}
              />
              <NumberField
                id="weight"
                label="Weight (kg)"
                error={errors.weightKg?.message}
                {...register('weightKg')}
              />
              <NumberField
                id="pain"
                label="Pain (0–10)"
                error={errors.painScale?.message}
                {...register('painScale')}
              />
              <div className="space-y-2 sm:col-span-2 lg:col-span-3">
                <Label htmlFor="vitals-notes">Notes</Label>
                <Textarea id="vitals-notes" rows={2} {...register('notes')} />
                <FieldError message={errors.notes?.message} />
              </div>
            </div>
            <div className="flex justify-end">
              <Button type="submit" disabled={busy}>
                {busy ? <Loader2Icon className="animate-spin" data-icon="inline-start" /> : null}
                Record vitals
              </Button>
            </div>
          </form>
        ) : null}
      </Can>

      {vitalsQuery.isError ? (
        <EmptyState
          title="Unable to load vitals"
          description={getErrorMessage(vitalsQuery.error)}
        />
      ) : vitalsQuery.isLoading ? (
        <div className="text-muted-foreground py-8 text-center text-sm">Loading vitals…</div>
      ) : rows.length === 0 ? (
        <EmptyState
          title="No vital signs yet"
          description="Record the first measurement for this consultation."
        />
      ) : (
        <div className="overflow-x-auto rounded-xl border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Recorded</TableHead>
                <TableHead>Temp</TableHead>
                <TableHead>HR</TableHead>
                <TableHead>BP</TableHead>
                <TableHead>SpO₂</TableHead>
                <TableHead>BMI</TableHead>
                <TableHead>Pain</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {rows.map((row) => (
                <TableRow key={row.id}>
                  <TableCell className="whitespace-nowrap">
                    {formatInstant(row.recordedAt)}
                  </TableCell>
                  <TableCell>{row.temperatureCelsius ?? '—'}</TableCell>
                  <TableCell>{row.heartRateBpm ?? '—'}</TableCell>
                  <TableCell>
                    {formatBp(
                      row.bloodPressure?.systolicMmHg,
                      row.bloodPressure?.diastolicMmHg,
                    )}
                  </TableCell>
                  <TableCell>{row.oxygenSaturationPercent ?? '—'}</TableCell>
                  <TableCell>{row.bmi ?? '—'}</TableCell>
                  <TableCell>{row.painScale ?? '—'}</TableCell>
                  <TableCell className="text-right">
                    <Can permissions={[Permissions.VISIT_DELETE]}>
                      {editable ? (
                        <Button
                          type="button"
                          size="sm"
                          variant="ghost"
                          onClick={() => void onDelete(row.id)}
                          disabled={mutations.remove.isPending}
                        >
                          <Trash2Icon data-icon="inline-start" />
                          Delete
                        </Button>
                      ) : null}
                    </Can>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}
    </div>
  );
}

type NumberFieldProps = {
  id: string;
  label: string;
  error?: string;
} & React.InputHTMLAttributes<HTMLInputElement>;

function NumberField({ id, label, error, ...props }: NumberFieldProps) {
  return (
    <div className="space-y-2">
      <Label htmlFor={id}>{label}</Label>
      <Input id={id} type="number" step="any" inputMode="decimal" {...props} />
      <FieldError message={error} />
    </div>
  );
}
