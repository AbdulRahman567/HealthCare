'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2Icon, PencilIcon, PlusIcon, Trash2Icon } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useForm, type UseFormReturn } from 'react-hook-form';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
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
import { SeverityBadge } from '@/features/patients/components/shared/severity-badge';
import { useAllergiesQuery, useAllergyMutations } from '@/features/patients/hooks/use-allergies';
import { emptyToNull, formatDate } from '@/features/patients/lib/patient-format';
import type { AllergyResponse } from '@/features/patients/types/allergy';
import {
  ALLERGY_REACTIONS,
  ALLERGY_SEVERITIES,
  ALLERGY_STATUSES,
  ALLERGY_TYPES,
  type AllergyType,
} from '@/features/patients/types/enums';
import {
  allergyFormSchema,
  emptyAllergyForm,
  type AllergyFormValues,
} from '@/features/patients/validation/allergy-schema';
import { getErrorMessage } from '@/lib/api-error';
import { formatEnumLabel } from '@/lib/page-query';

type AllergiesPanelProps = {
  patientId: string;
};

export function AllergiesPanel({ patientId }: AllergiesPanelProps) {
  const [typeFilter, setTypeFilter] = useState('');
  const allergiesQuery = useAllergiesQuery(
    patientId,
    (typeFilter || undefined) as AllergyType | undefined,
  );
  const mutations = useAllergyMutations(patientId);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editing, setEditing] = useState<AllergyResponse | null>(null);
  const [deleting, setDeleting] = useState<AllergyResponse | null>(null);

  const form = useForm<AllergyFormValues>({
    resolver: zodResolver(allergyFormSchema),
    defaultValues: emptyAllergyForm(),
  });

  useEffect(() => {
    if (!dialogOpen) {
      return;
    }
    form.reset(
      editing
        ? {
            allergenName: editing.allergenName,
            allergenCode: editing.allergenCode ?? '',
            allergyType: editing.allergyType,
            severity: editing.severity,
            reaction: editing.reaction,
            status: editing.status,
            onsetDate: editing.onsetDate ?? '',
            clinicalNotes: editing.clinicalNotes ?? '',
            verified: editing.verified,
            patientReported: editing.patientReported,
            criticalAlert: editing.criticalAlert,
            showOnBanner: editing.showOnBanner,
          }
        : emptyAllergyForm(),
    );
  }, [dialogOpen, editing, form]);

  const isBusy =
    mutations.create.isPending || mutations.update.isPending || mutations.remove.isPending;

  const onSubmit = form.handleSubmit(async (values) => {
    const payload = {
      allergenName: values.allergenName.trim(),
      allergenCode: emptyToNull(values.allergenCode),
      allergyType: values.allergyType,
      severity: values.severity,
      reaction: values.reaction,
      status: values.status,
      onsetDate: emptyToNull(values.onsetDate),
      clinicalNotes: emptyToNull(values.clinicalNotes),
      verified: values.verified,
      patientReported: values.patientReported,
      criticalAlert: values.criticalAlert,
      showOnBanner: values.showOnBanner,
    };
    try {
      if (editing) {
        await mutations.update.mutateAsync({ allergyId: editing.id, payload });
        toast.success('Allergy updated');
      } else {
        await mutations.create.mutateAsync(payload);
        toast.success('Allergy added');
      }
      setDialogOpen(false);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to save allergy'));
    }
  });

  const onDelete = async () => {
    if (!deleting) {
      return;
    }
    try {
      await mutations.remove.mutateAsync(deleting.id);
      toast.success('Allergy removed');
      setDeleting(null);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to remove allergy'));
    }
  };

  const rows = allergiesQuery.data ?? [];

  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <FilterSelect
          value={typeFilter}
          onValueChange={setTypeFilter}
          options={ALLERGY_TYPES}
          placeholder="Type"
          allLabel="All types"
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
            Add allergy
          </Button>
        </Can>
      </div>

      {allergiesQuery.isError ? (
        <EmptyState
          title="Unable to load allergies"
          description={getErrorMessage(allergiesQuery.error)}
        />
      ) : allergiesQuery.isLoading ? (
        <div className="text-muted-foreground py-10 text-center text-sm">Loading allergies…</div>
      ) : rows.length === 0 ? (
        <EmptyState
          title="No allergies recorded"
          description="Document known allergies so they appear on the safety banner."
        />
      ) : (
        <div className="overflow-x-auto rounded-xl border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Allergen</TableHead>
                <TableHead>Type</TableHead>
                <TableHead>Severity</TableHead>
                <TableHead>Reaction</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Onset</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {rows.map((allergy) => (
                <TableRow key={allergy.id}>
                  <TableCell className="font-medium">
                    <div className="flex flex-col gap-1">
                      <span>{allergy.allergenName}</span>
                      {allergy.criticalAlert || allergy.lifeThreatening ? (
                        <span className="text-destructive text-xs font-medium">Critical alert</span>
                      ) : null}
                    </div>
                  </TableCell>
                  <TableCell>{formatEnumLabel(allergy.allergyType)}</TableCell>
                  <TableCell>
                    <SeverityBadge severity={allergy.severity} />
                  </TableCell>
                  <TableCell>{formatEnumLabel(allergy.reaction)}</TableCell>
                  <TableCell>
                    <StatusBadge status={allergy.status} />
                  </TableCell>
                  <TableCell>{formatDate(allergy.onsetDate)}</TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-1">
                      <Can permissions={[Permissions.PATIENT_UPDATE]}>
                        <Button
                          type="button"
                          variant="ghost"
                          size="icon-sm"
                          aria-label={`Edit ${allergy.allergenName}`}
                          onClick={() => {
                            setEditing(allergy);
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
                          aria-label={`Remove ${allergy.allergenName}`}
                          onClick={() => setDeleting(allergy)}
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

      <AllergyDialog
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
            <DialogTitle>Remove allergy</DialogTitle>
            <DialogDescription>
              Soft-delete <span className="font-medium">{deleting?.allergenName}</span>? Safety
              history is retained for audit.
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

function AllergyDialog({
  open,
  onOpenChange,
  form,
  editing,
  isBusy,
  onSubmit,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  form: UseFormReturn<AllergyFormValues>;
  editing: boolean;
  isBusy: boolean;
  onSubmit: () => void;
}) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-lg" showCloseButton>
        <DialogHeader>
          <DialogTitle>{editing ? 'Edit allergy' : 'Add allergy'}</DialogTitle>
          <DialogDescription>
            Life-threatening severity or anaphylaxis automatically escalates critical banner flags.
          </DialogDescription>
        </DialogHeader>
        <form className="grid gap-4 sm:grid-cols-2" onSubmit={onSubmit} noValidate>
          <div className="space-y-2 sm:col-span-2">
            <Label htmlFor="allergen-name">Allergen</Label>
            <Input id="allergen-name" {...form.register('allergenName')} />
            <FieldError message={form.formState.errors.allergenName?.message} />
          </div>
          <div className="space-y-2">
            <Label htmlFor="allergen-code">Code</Label>
            <Input id="allergen-code" {...form.register('allergenCode')} />
          </div>
          <EnumSelect
            label="Type"
            value={form.watch('allergyType')}
            onValueChange={(value) =>
              form.setValue('allergyType', value as AllergyFormValues['allergyType'], {
                shouldValidate: true,
              })
            }
            options={ALLERGY_TYPES}
          />
          <EnumSelect
            label="Severity"
            value={form.watch('severity')}
            onValueChange={(value) =>
              form.setValue('severity', value as AllergyFormValues['severity'], {
                shouldValidate: true,
              })
            }
            options={ALLERGY_SEVERITIES}
          />
          <EnumSelect
            label="Reaction"
            value={form.watch('reaction')}
            onValueChange={(value) =>
              form.setValue('reaction', value as AllergyFormValues['reaction'], {
                shouldValidate: true,
              })
            }
            options={ALLERGY_REACTIONS}
          />
          <EnumSelect
            label="Status"
            value={form.watch('status')}
            onValueChange={(value) =>
              form.setValue('status', value as AllergyFormValues['status'], {
                shouldValidate: true,
              })
            }
            options={ALLERGY_STATUSES}
          />
          <div className="space-y-2">
            <Label htmlFor="onset-date">Onset date</Label>
            <Input id="onset-date" type="date" {...form.register('onsetDate')} />
          </div>
          <div className="space-y-2 sm:col-span-2">
            <Label htmlFor="allergy-notes">Clinical notes</Label>
            <Textarea id="allergy-notes" rows={3} {...form.register('clinicalNotes')} />
          </div>
          <FlagCheckbox
            id="verified"
            label="Clinician verified"
            checked={form.watch('verified')}
            onCheckedChange={(checked) => form.setValue('verified', checked)}
          />
          <FlagCheckbox
            id="patient-reported"
            label="Patient reported"
            checked={form.watch('patientReported')}
            onCheckedChange={(checked) => form.setValue('patientReported', checked)}
          />
          <FlagCheckbox
            id="critical-alert"
            label="Critical alert"
            checked={form.watch('criticalAlert')}
            onCheckedChange={(checked) => form.setValue('criticalAlert', checked)}
          />
          <FlagCheckbox
            id="show-banner"
            label="Show on banner"
            checked={form.watch('showOnBanner')}
            onCheckedChange={(checked) => form.setValue('showOnBanner', checked)}
          />
          <DialogFooter className="sm:col-span-2">
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Cancel
            </Button>
            <Button type="submit" disabled={isBusy}>
              {isBusy ? <Loader2Icon className="animate-spin" data-icon="inline-start" /> : null}
              Save allergy
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

function FlagCheckbox({
  id,
  label,
  checked,
  onCheckedChange,
}: {
  id: string;
  label: string;
  checked: boolean;
  onCheckedChange: (checked: boolean) => void;
}) {
  return (
    <label htmlFor={id} className="flex items-center gap-2 text-sm">
      <Checkbox
        id={id}
        checked={checked}
        onCheckedChange={(value) => onCheckedChange(value === true)}
      />
      {label}
    </label>
  );
}
