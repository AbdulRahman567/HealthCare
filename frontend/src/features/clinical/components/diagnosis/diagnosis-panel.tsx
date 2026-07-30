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
import {
  useConsultationDiagnosesQuery,
  useDiagnosisMutations,
} from '@/features/clinical/hooks/use-diagnoses';
import { useAutoSaveDraft } from '@/features/clinical/hooks/use-auto-save-draft';
import { toDiagnosisPayload } from '@/features/clinical/lib/clinical-format';
import {
  DIAGNOSIS_SEVERITIES,
  DIAGNOSIS_STATUSES,
  DIAGNOSIS_TYPES,
} from '@/features/clinical/types/enums';
import {
  diagnosisFormSchema,
  emptyDiagnosisForm,
  type DiagnosisFormValues,
} from '@/features/clinical/validation/clinical-schemas';
import { EmptyState } from '@/features/hospital-admin/components/shared/empty-state';
import { StatusBadge } from '@/features/hospital-admin/components/shared/status-badge';
import { EnumSelect, FieldError } from '@/features/patients/components/shared/enum-select';
import { getErrorMessage } from '@/lib/api-error';
import { formatEnumLabel } from '@/lib/page-query';

type DiagnosisPanelProps = {
  consultationId: string;
  patientId: string;
  editable: boolean;
};

export function DiagnosisPanel({ consultationId, patientId, editable }: DiagnosisPanelProps) {
  const listQuery = useConsultationDiagnosesQuery(consultationId);
  const mutations = useDiagnosisMutations(consultationId, patientId);
  const form = useForm<DiagnosisFormValues>({
    resolver: zodResolver(diagnosisFormSchema),
    defaultValues: emptyDiagnosisForm(),
  });

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors },
  } = form;

  const { status, lastSavedAt, clearSavedDraft } = useAutoSaveDraft({
    consultationId,
    tab: 'diagnosis',
    watch,
    enabled: editable,
    onRestore: (values) => reset(values),
  });

  const onSubmit = handleSubmit(async (values) => {
    try {
      await mutations.create.mutateAsync(toDiagnosisPayload(values));
      clearSavedDraft();
      reset(emptyDiagnosisForm());
      toast.success('Diagnosis added');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to add diagnosis'));
    }
  });

  const onDelete = async (id: string) => {
    try {
      await mutations.remove.mutateAsync(id);
      toast.success('Diagnosis removed');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to delete diagnosis'));
    }
  };

  const rows = listQuery.data ?? [];

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-base font-semibold">Diagnoses</h2>
        <p className="text-muted-foreground text-xs">
          Structured ICD-capable diagnoses. Only one PRIMARY diagnosis per consultation.
        </p>
      </div>

      <Can permissions={[Permissions.VISIT_UPDATE]}>
        {editable ? (
          <form onSubmit={onSubmit} noValidate className="space-y-4 rounded-xl border bg-card p-4">
            <div className="flex items-center justify-between gap-2">
              <h3 className="text-sm font-medium">Add diagnosis</h3>
              <DraftSaveIndicator status={status} lastSavedAt={lastSavedAt} />
            </div>
            <div className="grid gap-3 sm:grid-cols-2">
              <div className="space-y-2 sm:col-span-2">
                <Label htmlFor="dx-name">Diagnosis name</Label>
                <Input id="dx-name" autoFocus {...register('diagnosisName')} />
                <FieldError message={errors.diagnosisName?.message} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="dx-icd">ICD-10 code</Label>
                <Input id="dx-icd" placeholder="e.g. J06.9" {...register('icdCode')} />
                <FieldError message={errors.icdCode?.message} />
              </div>
              <EnumSelect
                id="dx-type"
                label="Type"
                value={watch('diagnosisType')}
                onValueChange={(value) =>
                  setValue('diagnosisType', value as DiagnosisFormValues['diagnosisType'], {
                    shouldValidate: true,
                  })
                }
                options={DIAGNOSIS_TYPES}
                error={errors.diagnosisType?.message}
              />
              <EnumSelect
                id="dx-status"
                label="Status"
                value={watch('diagnosisStatus')}
                onValueChange={(value) =>
                  setValue('diagnosisStatus', value as DiagnosisFormValues['diagnosisStatus'], {
                    shouldValidate: true,
                  })
                }
                options={DIAGNOSIS_STATUSES}
                error={errors.diagnosisStatus?.message}
              />
              <EnumSelect
                id="dx-severity"
                label="Severity"
                value={watch('severity')}
                onValueChange={(value) =>
                  setValue('severity', value as DiagnosisFormValues['severity'], {
                    shouldValidate: true,
                  })
                }
                options={DIAGNOSIS_SEVERITIES}
                error={errors.severity?.message}
              />
              <div className="space-y-2 sm:col-span-2">
                <Label htmlFor="dx-notes">Clinical notes</Label>
                <Textarea id="dx-notes" rows={2} {...register('clinicalNotes')} />
                <FieldError message={errors.clinicalNotes?.message} />
              </div>
            </div>
            <div className="flex justify-end">
              <Button type="submit" disabled={mutations.create.isPending}>
                {mutations.create.isPending ? (
                  <Loader2Icon className="animate-spin" data-icon="inline-start" />
                ) : null}
                Add diagnosis
              </Button>
            </div>
          </form>
        ) : null}
      </Can>

      {listQuery.isError ? (
        <EmptyState
          title="Unable to load diagnoses"
          description={getErrorMessage(listQuery.error)}
        />
      ) : listQuery.isLoading ? (
        <div className="text-muted-foreground py-8 text-center text-sm">Loading diagnoses…</div>
      ) : rows.length === 0 ? (
        <EmptyState
          title="No diagnoses yet"
          description="Add a primary or differential diagnosis for this encounter."
        />
      ) : (
        <div className="overflow-x-auto rounded-xl border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>#</TableHead>
                <TableHead>Diagnosis</TableHead>
                <TableHead>ICD</TableHead>
                <TableHead>Type</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Severity</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {rows.map((row) => (
                <TableRow key={row.id}>
                  <TableCell>{row.sequenceNumber}</TableCell>
                  <TableCell className="font-medium">{row.diagnosisName}</TableCell>
                  <TableCell>{row.icdCode || '—'}</TableCell>
                  <TableCell>{formatEnumLabel(row.diagnosisType)}</TableCell>
                  <TableCell>
                    <StatusBadge status={row.diagnosisStatus} />
                  </TableCell>
                  <TableCell>
                    <StatusBadge status={row.severity} />
                  </TableCell>
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
