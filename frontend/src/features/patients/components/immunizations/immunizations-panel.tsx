'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2Icon, PencilIcon, PlusIcon, Trash2Icon } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useForm, type UseFormReturn } from 'react-hook-form';
import { toast } from 'sonner';

import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
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
import { EmptyState } from '@/features/hospital-admin/components/shared/empty-state';
import { FilterSelect } from '@/features/hospital-admin/components/shared/filter-select';
import { StatusBadge } from '@/features/hospital-admin/components/shared/status-badge';
import { EnumSelect, FieldError } from '@/features/patients/components/shared/enum-select';
import {
  useImmunizationMutations,
  useImmunizationsDueQuery,
  useImmunizationsQuery,
} from '@/features/patients/hooks/use-immunizations';
import { emptyToNull, formatDate } from '@/features/patients/lib/patient-format';
import {
  IMMUNIZATION_STATUSES,
  VACCINE_ROUTES,
  type ImmunizationStatus,
} from '@/features/patients/types/enums';
import type {
  ImmunizationResponse,
  UpsertImmunizationPayload,
} from '@/features/patients/types/immunization';
import {
  emptyImmunizationForm,
  immunizationFormSchema,
  type ImmunizationFormValues,
} from '@/features/patients/validation/immunization-schema';
import { getErrorMessage } from '@/lib/api-error';

type ImmunizationsPanelProps = {
  patientId: string;
};

export function ImmunizationsPanel({ patientId }: ImmunizationsPanelProps) {
  const [statusFilter, setStatusFilter] = useState('');
  const listQuery = useImmunizationsQuery(
    patientId,
    (statusFilter || undefined) as ImmunizationStatus | undefined,
  );
  const dueQuery = useImmunizationsDueQuery(patientId);
  const mutations = useImmunizationMutations(patientId);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState<ImmunizationResponse | null>(null);
  const [deleting, setDeleting] = useState<ImmunizationResponse | null>(null);

  const form = useForm<ImmunizationFormValues>({
    resolver: zodResolver(immunizationFormSchema),
    defaultValues: emptyImmunizationForm(),
  });

  useEffect(() => {
    if (!dialogOpen) {
      return;
    }
    form.reset(
      editing
        ? {
            vaccineName: editing.vaccineName,
            vaccineCode: editing.vaccineCode ?? '',
            doseNumber: String(editing.doseNumber),
            manufacturer: editing.manufacturer ?? '',
            batchNumber: editing.batchNumber ?? '',
            administrationDate: editing.administrationDate,
            nextDueDate: editing.nextDueDate ?? '',
            healthcareProvider: editing.healthcareProvider,
            route: editing.route ?? '',
            status: editing.status,
            clinicalNotes: editing.clinicalNotes ?? '',
          }
        : emptyImmunizationForm(),
    );
  }, [dialogOpen, editing, form]);

  const isBusy =
    mutations.create.isPending || mutations.update.isPending || mutations.remove.isPending;

  const onSubmit = form.handleSubmit(async (values) => {
    const payload = {
      vaccineName: values.vaccineName.trim(),
      vaccineCode: emptyToNull(values.vaccineCode),
      doseNumber: Number(values.doseNumber),
      manufacturer: emptyToNull(values.manufacturer),
      batchNumber: emptyToNull(values.batchNumber),
      administrationDate: values.administrationDate,
      nextDueDate: emptyToNull(values.nextDueDate),
      healthcareProvider: values.healthcareProvider.trim(),
      route: values.route
        ? (values.route as NonNullable<UpsertImmunizationPayload['route']>)
        : null,
      status: values.status,
      clinicalNotes: emptyToNull(values.clinicalNotes),
    };
    try {
      if (editing) {
        await mutations.update.mutateAsync({ immunizationId: editing.id, payload });
        toast.success('Vaccination updated');
      } else {
        await mutations.create.mutateAsync(payload);
        toast.success('Vaccination recorded');
      }
      setDialogOpen(false);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to save vaccination'));
    }
  });

  const onDelete = async () => {
    if (!deleting) {
      return;
    }
    try {
      await mutations.remove.mutateAsync(deleting.id);
      toast.success('Vaccination removed');
      setDeleting(null);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to remove vaccination'));
    }
  };

  const rows = listQuery.data ?? [];
  const dueCount = dueQuery.data?.dueCount ?? 0;

  return (
    <div className="space-y-4">
      {dueCount > 0 ? (
        <Alert>
          <AlertTitle>Doses due / overdue</AlertTitle>
          <AlertDescription>
            {dueCount} immunization{dueCount === 1 ? '' : 's'} with next due date on or before
            today.
          </AlertDescription>
        </Alert>
      ) : null}

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <FilterSelect
          value={statusFilter}
          onValueChange={setStatusFilter}
          options={IMMUNIZATION_STATUSES}
          placeholder="Status"
          allLabel="All statuses"
        />
        <Can permissions={[Permissions.PATIENT_UPDATE]}>
          <Button
            type="button"
            size="sm"
            onClick={() => {
              setEditing(null);
              setDialogOpen(true);
            }}
          >
            <PlusIcon data-icon="inline-start" />
            Record vaccination
          </Button>
        </Can>
      </div>

      {listQuery.isError ? (
        <EmptyState
          title="Unable to load vaccinations"
          description={getErrorMessage(listQuery.error)}
        />
      ) : listQuery.isLoading ? (
        <div className="text-muted-foreground py-10 text-center text-sm">Loading vaccinations…</div>
      ) : rows.length === 0 ? (
        <EmptyState
          title="No vaccinations recorded"
          description="Capture dose, lot, and next-due details for preventive care follow-up."
        />
      ) : (
        <div className="overflow-x-auto rounded-xl border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Vaccine</TableHead>
                <TableHead>Dose</TableHead>
                <TableHead>Administered</TableHead>
                <TableHead>Next due</TableHead>
                <TableHead>Provider</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {rows.map((item) => (
                <TableRow key={item.id}>
                  <TableCell className="font-medium">
                    <div className="flex flex-wrap items-center gap-2">
                      <span>{item.vaccineName}</span>
                      {item.due ? <Badge variant="secondary">Due</Badge> : null}
                    </div>
                    {item.batchNumber ? (
                      <p className="text-muted-foreground text-xs">Lot {item.batchNumber}</p>
                    ) : null}
                  </TableCell>
                  <TableCell>{item.doseNumber}</TableCell>
                  <TableCell>{formatDate(item.administrationDate)}</TableCell>
                  <TableCell>{formatDate(item.nextDueDate)}</TableCell>
                  <TableCell>{item.healthcareProvider}</TableCell>
                  <TableCell>
                    <StatusBadge status={item.status} />
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-1">
                      <Can permissions={[Permissions.PATIENT_UPDATE]}>
                        <Button
                          type="button"
                          variant="ghost"
                          size="icon-sm"
                          aria-label={`Edit ${item.vaccineName}`}
                          onClick={() => {
                            setEditing(item);
                            setDialogOpen(true);
                          }}
                        >
                          <PencilIcon />
                        </Button>
                      </Can>
                      <Can permissions={[Permissions.PATIENT_DELETE]}>
                        <Button
                          type="button"
                          variant="ghost"
                          size="icon-sm"
                          aria-label={`Remove ${item.vaccineName}`}
                          onClick={() => setDeleting(item)}
                        >
                          <Trash2Icon />
                        </Button>
                      </Can>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      <ImmunizationDialog
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        form={form}
        editing={Boolean(editing)}
        isBusy={isBusy}
        onSubmit={onSubmit}
      />

      <Dialog open={Boolean(deleting)} onOpenChange={(open) => !open && setDeleting(null)}>
        <DialogContent showCloseButton>
          <DialogHeader>
            <DialogTitle>Remove vaccination</DialogTitle>
            <DialogDescription>
              Soft-delete <span className="font-medium">{deleting?.vaccineName}</span> dose{' '}
              {deleting?.doseNumber}?
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => setDeleting(null)}>
              Cancel
            </Button>
            <Button type="button" variant="destructive" disabled={isBusy} onClick={onDelete}>
              Remove
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

function ImmunizationDialog({
  open,
  onOpenChange,
  form,
  editing,
  isBusy,
  onSubmit,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  form: UseFormReturn<ImmunizationFormValues>;
  editing: boolean;
  isBusy: boolean;
  onSubmit: () => void;
}) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg" showCloseButton>
        <DialogHeader>
          <DialogTitle>{editing ? 'Edit vaccination' : 'Record vaccination'}</DialogTitle>
          <DialogDescription>
            Include lot/batch when available for recall readiness and series tracking.
          </DialogDescription>
        </DialogHeader>
        <form className="grid gap-4 sm:grid-cols-2" onSubmit={onSubmit} noValidate>
          <div className="space-y-2 sm:col-span-2">
            <Label htmlFor="vaccine-name">Vaccine</Label>
            <Input id="vaccine-name" {...form.register('vaccineName')} />
            <FieldError message={form.formState.errors.vaccineName?.message} />
          </div>
          <div className="space-y-2">
            <Label htmlFor="vaccine-code">Code</Label>
            <Input id="vaccine-code" {...form.register('vaccineCode')} />
          </div>
          <div className="space-y-2">
            <Label htmlFor="dose-number">Dose number</Label>
            <Input id="dose-number" inputMode="numeric" {...form.register('doseNumber')} />
            <FieldError message={form.formState.errors.doseNumber?.message} />
          </div>
          <div className="space-y-2">
            <Label htmlFor="manufacturer">Manufacturer</Label>
            <Input id="manufacturer" {...form.register('manufacturer')} />
          </div>
          <div className="space-y-2">
            <Label htmlFor="batch-number">Batch / lot</Label>
            <Input id="batch-number" {...form.register('batchNumber')} />
          </div>
          <div className="space-y-2">
            <Label htmlFor="admin-date">Administration date</Label>
            <Input id="admin-date" type="date" {...form.register('administrationDate')} />
            <FieldError message={form.formState.errors.administrationDate?.message} />
          </div>
          <div className="space-y-2">
            <Label htmlFor="next-due">Next due date</Label>
            <Input id="next-due" type="date" {...form.register('nextDueDate')} />
            <FieldError message={form.formState.errors.nextDueDate?.message} />
          </div>
          <div className="space-y-2 sm:col-span-2">
            <Label htmlFor="provider">Healthcare provider</Label>
            <Input id="provider" {...form.register('healthcareProvider')} />
            <FieldError message={form.formState.errors.healthcareProvider?.message} />
          </div>
          <EnumSelect
            label="Route"
            value={form.watch('route') || ''}
            onValueChange={(value) =>
              form.setValue('route', value as ImmunizationFormValues['route'], {
                shouldValidate: true,
              })
            }
            options={VACCINE_ROUTES}
            optional
          />
          <EnumSelect
            label="Status"
            value={form.watch('status')}
            onValueChange={(value) =>
              form.setValue('status', value as ImmunizationFormValues['status'], {
                shouldValidate: true,
              })
            }
            options={IMMUNIZATION_STATUSES}
          />
          <div className="space-y-2 sm:col-span-2">
            <Label htmlFor="imm-notes">Clinical notes</Label>
            <Textarea id="imm-notes" rows={3} {...form.register('clinicalNotes')} />
          </div>
          <DialogFooter className="sm:col-span-2">
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={isBusy}>
              {isBusy ? <Loader2Icon className="animate-spin" data-icon="inline-start" /> : null}
              Save vaccination
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
