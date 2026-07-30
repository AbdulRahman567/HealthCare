'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2Icon, PlusIcon, PrinterIcon, Trash2Icon } from 'lucide-react';
import Link from 'next/link';
import { useFieldArray, useForm } from 'react-hook-form';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
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
  useConsultationPrescriptionsQuery,
  usePrescriptionMutations,
} from '@/features/clinical/hooks/use-prescriptions';
import { toCreatePrescriptionPayload } from '@/features/clinical/lib/clinical-format';
import { MEDICATION_ROUTES } from '@/features/clinical/types/enums';
import {
  emptyPrescriptionForm,
  emptyPrescriptionItem,
  prescriptionFormSchema,
  type PrescriptionFormValues,
} from '@/features/clinical/validation/clinical-schemas';
import { EmptyState } from '@/features/hospital-admin/components/shared/empty-state';
import { StatusBadge } from '@/features/hospital-admin/components/shared/status-badge';
import { EnumSelect, FieldError } from '@/features/patients/components/shared/enum-select';
import { getErrorMessage } from '@/lib/api-error';
import { formatEnumLabel } from '@/lib/page-query';

type PrescriptionsPanelProps = {
  consultationId: string;
  patientId: string;
  editable: boolean;
};

export function PrescriptionsPanel({
  consultationId,
  patientId,
  editable,
}: PrescriptionsPanelProps) {
  const listQuery = useConsultationPrescriptionsQuery(consultationId);
  const mutations = usePrescriptionMutations(consultationId, patientId);
  const form = useForm<PrescriptionFormValues>({
    resolver: zodResolver(prescriptionFormSchema),
    defaultValues: emptyPrescriptionForm(),
  });

  const {
    register,
    control,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors },
  } = form;

  const { fields, append, remove } = useFieldArray({ control, name: 'items' });

  const { status, lastSavedAt, clearSavedDraft } = useAutoSaveDraft({
    consultationId,
    tab: 'prescriptions',
    watch,
    enabled: editable,
    onRestore: (values) => reset(values),
  });

  const onSubmit = handleSubmit(async (values) => {
    try {
      await mutations.create.mutateAsync(toCreatePrescriptionPayload(consultationId, values));
      clearSavedDraft();
      reset(emptyPrescriptionForm());
      toast.success(values.issueImmediately ? 'Prescription issued' : 'Prescription draft saved');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to create prescription'));
    }
  });

  const onIssue = async (id: string) => {
    try {
      await mutations.issue.mutateAsync(id);
      toast.success('Prescription issued');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to issue prescription'));
    }
  };

  const onDelete = async (id: string) => {
    try {
      await mutations.remove.mutateAsync(id);
      toast.success('Draft prescription deleted');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to delete prescription'));
    }
  };

  const onCancel = async (id: string) => {
    const reason = window.prompt('Cancel reason (optional)');
    if (reason === null) {
      return;
    }
    try {
      await mutations.cancel.mutateAsync({
        id,
        payload: reason.trim() ? { reason: reason.trim() } : {},
      });
      toast.success('Prescription cancelled');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to cancel prescription'));
    }
  };

  const rows = listQuery.data ?? [];
  const canCancelStatus = (status: string) =>
    status === 'DRAFT' || status === 'ISSUED' || status === 'PARTIALLY_DISPENSED';

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-base font-semibold">Prescriptions</h2>
        <p className="text-muted-foreground text-xs">
          Write medicine lines for this encounter. Drafts can be issued when ready for pharmacy.
        </p>
      </div>

      <Can permissions={[Permissions.PRESCRIPTION_CREATE]}>
        {editable ? (
          <form onSubmit={onSubmit} noValidate className="space-y-4 rounded-xl border bg-card p-4">
            <div className="flex items-center justify-between gap-2">
              <h3 className="text-sm font-medium">New prescription</h3>
              <DraftSaveIndicator status={status} lastSavedAt={lastSavedAt} />
            </div>

            <div className="space-y-4">
              {fields.map((field, index) => (
                <div
                  key={field.id}
                  className="grid gap-3 rounded-lg border border-dashed p-3 sm:grid-cols-2 lg:grid-cols-3"
                >
                  <div className="space-y-2 sm:col-span-2 lg:col-span-3">
                    <div className="flex items-center justify-between">
                      <Label htmlFor={`rx-med-${index}`}>Medicine {index + 1}</Label>
                      {fields.length > 1 ? (
                        <Button
                          type="button"
                          size="sm"
                          variant="ghost"
                          onClick={() => remove(index)}
                        >
                          <Trash2Icon data-icon="inline-start" />
                          Remove
                        </Button>
                      ) : null}
                    </div>
                    <Input id={`rx-med-${index}`} {...register(`items.${index}.medicineName`)} />
                    <FieldError message={errors.items?.[index]?.medicineName?.message} />
                  </div>
                  <div className="space-y-2">
                    <Label>Dosage</Label>
                    <Input {...register(`items.${index}.dosage`)} />
                    <FieldError message={errors.items?.[index]?.dosage?.message} />
                  </div>
                  <div className="space-y-2">
                    <Label>Frequency</Label>
                    <Input {...register(`items.${index}.frequency`)} />
                    <FieldError message={errors.items?.[index]?.frequency?.message} />
                  </div>
                  <EnumSelect
                    label="Route"
                    value={watch(`items.${index}.route`)}
                    onValueChange={(value) =>
                      setValue(
                        `items.${index}.route`,
                        value as PrescriptionFormValues['items'][number]['route'],
                        { shouldValidate: true },
                      )
                    }
                    options={MEDICATION_ROUTES}
                    error={errors.items?.[index]?.route?.message}
                  />
                  <div className="space-y-2">
                    <Label>Duration</Label>
                    <Input {...register(`items.${index}.duration`)} />
                    <FieldError message={errors.items?.[index]?.duration?.message} />
                  </div>
                  <div className="space-y-2">
                    <Label>Quantity</Label>
                    <Input type="number" min={1} {...register(`items.${index}.quantity`)} />
                    <FieldError message={errors.items?.[index]?.quantity?.message} />
                  </div>
                  <div className="space-y-2">
                    <Label>Refills</Label>
                    <Input type="number" min={0} {...register(`items.${index}.refills`)} />
                    <FieldError message={errors.items?.[index]?.refills?.message} />
                  </div>
                  <div className="space-y-2 sm:col-span-2 lg:col-span-3">
                    <Label>Instructions</Label>
                    <Input {...register(`items.${index}.instructions`)} />
                    <FieldError message={errors.items?.[index]?.instructions?.message} />
                  </div>
                  <label className="flex items-center gap-2 text-sm">
                    <Checkbox
                      checked={watch(`items.${index}.beforeFood`)}
                      onCheckedChange={(checked) =>
                        setValue(`items.${index}.beforeFood`, checked === true)
                      }
                    />
                    Before food
                  </label>
                  <label className="flex items-center gap-2 text-sm">
                    <Checkbox
                      checked={watch(`items.${index}.afterFood`)}
                      onCheckedChange={(checked) =>
                        setValue(`items.${index}.afterFood`, checked === true)
                      }
                    />
                    After food
                  </label>
                  <FieldError message={errors.items?.[index]?.beforeFood?.message} />
                </div>
              ))}
            </div>

            <div className="flex flex-wrap items-center justify-between gap-2">
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => append(emptyPrescriptionItem())}
              >
                <PlusIcon data-icon="inline-start" />
                Add medicine
              </Button>
              <label className="flex items-center gap-2 text-sm">
                <Checkbox
                  checked={watch('issueImmediately')}
                  onCheckedChange={(checked) => setValue('issueImmediately', checked === true)}
                />
                Issue immediately
              </label>
            </div>

            <div className="space-y-2">
              <Label htmlFor="rx-notes">Prescription notes</Label>
              <Textarea id="rx-notes" rows={2} {...register('notes')} />
              <FieldError message={errors.notes?.message} />
              <FieldError message={errors.items?.message ?? errors.items?.root?.message} />
            </div>

            <div className="flex justify-end">
              <Button type="submit" disabled={mutations.create.isPending}>
                {mutations.create.isPending ? (
                  <Loader2Icon className="animate-spin" data-icon="inline-start" />
                ) : null}
                Save prescription
              </Button>
            </div>
          </form>
        ) : null}
      </Can>

      {listQuery.isError ? (
        <EmptyState
          title="Unable to load prescriptions"
          description={getErrorMessage(listQuery.error)}
        />
      ) : listQuery.isLoading ? (
        <div className="text-muted-foreground py-8 text-center text-sm">Loading prescriptions…</div>
      ) : rows.length === 0 ? (
        <EmptyState
          title="No prescriptions yet"
          description="Create a draft or issue a prescription for this consultation."
        />
      ) : (
        <div className="space-y-3">
          {rows.map((rx) => (
            <div key={rx.id} className="rounded-xl border p-4">
              <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
                <div>
                  <p className="font-medium">{rx.prescriptionNumber}</p>
                  <p className="text-muted-foreground text-xs">{rx.prescriptionDate}</p>
                </div>
                <div className="flex flex-wrap items-center gap-2">
                  <StatusBadge status={rx.status} />
                  <Can permissions={[Permissions.PRESCRIPTION_READ]}>
                    <Button
                      nativeButton={false}
                      size="sm"
                      variant="outline"
                      render={<Link href={`/app/prescriptions/${rx.id}/print`} target="_blank" />}
                    >
                      <PrinterIcon data-icon="inline-start" />
                      Print
                    </Button>
                  </Can>
                  <Can permissions={[Permissions.PRESCRIPTION_UPDATE]}>
                    {editable && rx.status === 'DRAFT' ? (
                      <Button
                        type="button"
                        size="sm"
                        onClick={() => void onIssue(rx.id)}
                        disabled={mutations.issue.isPending}
                      >
                        Issue
                      </Button>
                    ) : null}
                  </Can>
                  <Can permissions={[Permissions.PRESCRIPTION_UPDATE]}>
                    {editable && canCancelStatus(rx.status) ? (
                      <Button
                        type="button"
                        size="sm"
                        variant="outline"
                        onClick={() => void onCancel(rx.id)}
                        disabled={mutations.cancel.isPending}
                      >
                        Cancel
                      </Button>
                    ) : null}
                  </Can>
                  <Can permissions={[Permissions.PRESCRIPTION_DELETE]}>
                    {editable && rx.status === 'DRAFT' ? (
                      <Button
                        type="button"
                        size="sm"
                        variant="ghost"
                        onClick={() => void onDelete(rx.id)}
                        disabled={mutations.remove.isPending}
                      >
                        <Trash2Icon data-icon="inline-start" />
                        Delete
                      </Button>
                    ) : null}
                  </Can>
                </div>
              </div>
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Medicine</TableHead>
                      <TableHead>Dosage</TableHead>
                      <TableHead>Frequency</TableHead>
                      <TableHead>Route</TableHead>
                      <TableHead>Duration</TableHead>
                      <TableHead>Qty</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {rx.items.map((item) => (
                      <TableRow key={item.id}>
                        <TableCell className="font-medium">{item.medicineName}</TableCell>
                        <TableCell>{item.dosage}</TableCell>
                        <TableCell>{item.frequency}</TableCell>
                        <TableCell>{formatEnumLabel(item.route)}</TableCell>
                        <TableCell>{item.duration}</TableCell>
                        <TableCell>{item.quantity}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
              {rx.notes ? <p className="text-muted-foreground mt-2 text-xs">{rx.notes}</p> : null}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
