'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2Icon, PencilIcon, PlusIcon, Trash2Icon } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useForm, type UseFormReturn } from 'react-hook-form';
import { toast } from 'sonner';

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
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Textarea } from '@/components/ui/textarea';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { Can } from '@/features/authorization/components/can';
import { Permissions } from '@/features/authorization/constants/permissions';
import { EmptyState } from '@/features/hospital-admin/components/shared/empty-state';
import { StatusBadge } from '@/features/hospital-admin/components/shared/status-badge';
import { EnumSelect, FieldError } from '@/features/patients/components/shared/enum-select';
import { SeverityBadge } from '@/features/patients/components/shared/severity-badge';
import {
  useMedicalHistoryMutations,
  useMedicalHistoryQuery,
} from '@/features/patients/hooks/use-medical-history';
import { emptyToNull, formatDate } from '@/features/patients/lib/patient-format';
import {
  CLINICAL_CONDITION_STATUSES,
  CLINICAL_SEVERITIES,
  DISEASE_CATEGORIES,
  FAMILY_RELATIONS,
  PROCEDURE_CATEGORIES,
} from '@/features/patients/types/enums';
import type {
  ChronicConditionResponse,
  FamilyHistoryResponse,
  PastDiseaseResponse,
  SurgeryHistoryResponse,
} from '@/features/patients/types/medical-history';
import {
  chronicConditionFormSchema,
  familyHistoryFormSchema,
  pastDiseaseFormSchema,
  surgeryFormSchema,
  type ChronicConditionFormValues,
  type FamilyHistoryFormValues,
  type PastDiseaseFormValues,
  type SurgeryFormValues,
} from '@/features/patients/validation/medical-history-schema';
import { getErrorMessage } from '@/lib/api-error';
import { formatEnumLabel } from '@/lib/page-query';

type MedicalHistoryPanelProps = {
  patientId: string;
};

type HistoryKind = 'disease' | 'surgery' | 'chronic' | 'family';

const emptyClinical = {
  diagnosisDate: '',
  recoveryDate: '',
  severity: 'MODERATE' as const,
  conditionStatus: 'ONGOING' as const,
  clinicalNotes: '',
};

export function MedicalHistoryPanel({ patientId }: MedicalHistoryPanelProps) {
  const historyQuery = useMedicalHistoryQuery(patientId);
  const mutations = useMedicalHistoryMutations(patientId);
  const [kind, setKind] = useState<HistoryKind>('disease');
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingDisease, setEditingDisease] = useState<PastDiseaseResponse | null>(null);
  const [editingSurgery, setEditingSurgery] = useState<SurgeryHistoryResponse | null>(null);
  const [editingChronic, setEditingChronic] = useState<ChronicConditionResponse | null>(null);
  const [editingFamily, setEditingFamily] = useState<FamilyHistoryResponse | null>(null);
  const [deleting, setDeleting] = useState<{ kind: HistoryKind; id: string; label: string } | null>(
    null,
  );

  const diseaseForm = useForm<PastDiseaseFormValues>({
    resolver: zodResolver(pastDiseaseFormSchema),
    defaultValues: {
      diseaseName: '',
      diseaseCategory: 'OTHER',
      diseaseCode: '',
      ...emptyClinical,
    },
  });
  const surgeryForm = useForm<SurgeryFormValues>({
    resolver: zodResolver(surgeryFormSchema),
    defaultValues: {
      procedureName: '',
      procedureCategory: 'GENERAL',
      procedureCode: '',
      performingFacility: '',
      ...emptyClinical,
    },
  });
  const chronicForm = useForm<ChronicConditionFormValues>({
    resolver: zodResolver(chronicConditionFormSchema),
    defaultValues: {
      conditionName: '',
      diseaseCategory: 'OTHER',
      conditionCode: '',
      ...emptyClinical,
    },
  });
  const familyForm = useForm<FamilyHistoryFormValues>({
    resolver: zodResolver(familyHistoryFormSchema),
    defaultValues: {
      diseaseName: '',
      diseaseCategory: 'OTHER',
      diseaseCode: '',
      familyRelation: 'OTHER',
      ...emptyClinical,
    },
  });

  useEffect(() => {
    if (!dialogOpen) {
      return;
    }
    if (kind === 'disease') {
      diseaseForm.reset(
        editingDisease
          ? {
              diseaseName: editingDisease.diseaseName,
              diseaseCategory: editingDisease.diseaseCategory,
              diseaseCode: editingDisease.diseaseCode ?? '',
              diagnosisDate: editingDisease.diagnosisDate,
              recoveryDate: editingDisease.recoveryDate ?? '',
              severity: editingDisease.severity,
              conditionStatus: editingDisease.conditionStatus,
              clinicalNotes: editingDisease.clinicalNotes ?? '',
            }
          : {
              diseaseName: '',
              diseaseCategory: 'OTHER',
              diseaseCode: '',
              ...emptyClinical,
            },
      );
    }
    if (kind === 'surgery') {
      surgeryForm.reset(
        editingSurgery
          ? {
              procedureName: editingSurgery.procedureName,
              procedureCategory: editingSurgery.procedureCategory,
              procedureCode: editingSurgery.procedureCode ?? '',
              performingFacility: editingSurgery.performingFacility ?? '',
              diagnosisDate: editingSurgery.diagnosisDate,
              recoveryDate: editingSurgery.recoveryDate ?? '',
              severity: editingSurgery.severity,
              conditionStatus: editingSurgery.conditionStatus,
              clinicalNotes: editingSurgery.clinicalNotes ?? '',
            }
          : {
              procedureName: '',
              procedureCategory: 'GENERAL',
              procedureCode: '',
              performingFacility: '',
              ...emptyClinical,
            },
      );
    }
    if (kind === 'chronic') {
      chronicForm.reset(
        editingChronic
          ? {
              conditionName: editingChronic.conditionName,
              diseaseCategory: editingChronic.diseaseCategory,
              conditionCode: editingChronic.conditionCode ?? '',
              diagnosisDate: editingChronic.diagnosisDate,
              recoveryDate: editingChronic.recoveryDate ?? '',
              severity: editingChronic.severity,
              conditionStatus: editingChronic.conditionStatus,
              clinicalNotes: editingChronic.clinicalNotes ?? '',
            }
          : {
              conditionName: '',
              diseaseCategory: 'OTHER',
              conditionCode: '',
              ...emptyClinical,
            },
      );
    }
    if (kind === 'family') {
      familyForm.reset(
        editingFamily
          ? {
              diseaseName: editingFamily.diseaseName,
              diseaseCategory: editingFamily.diseaseCategory,
              diseaseCode: editingFamily.diseaseCode ?? '',
              familyRelation: editingFamily.familyRelation,
              diagnosisDate: editingFamily.diagnosisDate,
              recoveryDate: editingFamily.recoveryDate ?? '',
              severity: editingFamily.severity,
              conditionStatus: editingFamily.conditionStatus,
              clinicalNotes: editingFamily.clinicalNotes ?? '',
            }
          : {
              diseaseName: '',
              diseaseCategory: 'OTHER',
              diseaseCode: '',
              familyRelation: 'OTHER',
              ...emptyClinical,
            },
      );
    }
  }, [
    dialogOpen,
    kind,
    editingDisease,
    editingSurgery,
    editingChronic,
    editingFamily,
    diseaseForm,
    surgeryForm,
    chronicForm,
    familyForm,
  ]);

  const openCreate = (nextKind: HistoryKind) => {
    setKind(nextKind);
    setEditingDisease(null);
    setEditingSurgery(null);
    setEditingChronic(null);
    setEditingFamily(null);
    setDialogOpen(true);
  };

  const isBusy =
    mutations.createPastDisease.isPending ||
    mutations.updatePastDisease.isPending ||
    mutations.deletePastDisease.isPending ||
    mutations.createSurgery.isPending ||
    mutations.updateSurgery.isPending ||
    mutations.deleteSurgery.isPending ||
    mutations.createChronicCondition.isPending ||
    mutations.updateChronicCondition.isPending ||
    mutations.deleteChronicCondition.isPending ||
    mutations.createFamilyHistory.isPending ||
    mutations.updateFamilyHistory.isPending ||
    mutations.deleteFamilyHistory.isPending;

  const onSubmitDisease = diseaseForm.handleSubmit(async (values) => {
    const payload = {
      diseaseName: values.diseaseName.trim(),
      diseaseCategory: values.diseaseCategory,
      diseaseCode: emptyToNull(values.diseaseCode),
      diagnosisDate: values.diagnosisDate,
      recoveryDate: emptyToNull(values.recoveryDate),
      severity: values.severity,
      conditionStatus: values.conditionStatus,
      clinicalNotes: emptyToNull(values.clinicalNotes),
    };
    try {
      if (editingDisease) {
        await mutations.updatePastDisease.mutateAsync({ entryId: editingDisease.id, payload });
        toast.success('Past disease updated');
      } else {
        await mutations.createPastDisease.mutateAsync(payload);
        toast.success('Past disease added');
      }
      setDialogOpen(false);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to save past disease'));
    }
  });

  const onSubmitSurgery = surgeryForm.handleSubmit(async (values) => {
    const payload = {
      procedureName: values.procedureName.trim(),
      procedureCategory: values.procedureCategory,
      procedureCode: emptyToNull(values.procedureCode),
      performingFacility: emptyToNull(values.performingFacility),
      diagnosisDate: values.diagnosisDate,
      recoveryDate: emptyToNull(values.recoveryDate),
      severity: values.severity,
      conditionStatus: values.conditionStatus,
      clinicalNotes: emptyToNull(values.clinicalNotes),
    };
    try {
      if (editingSurgery) {
        await mutations.updateSurgery.mutateAsync({ entryId: editingSurgery.id, payload });
        toast.success('Surgery updated');
      } else {
        await mutations.createSurgery.mutateAsync(payload);
        toast.success('Surgery added');
      }
      setDialogOpen(false);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to save surgery'));
    }
  });

  const onSubmitChronic = chronicForm.handleSubmit(async (values) => {
    const payload = {
      conditionName: values.conditionName.trim(),
      diseaseCategory: values.diseaseCategory,
      conditionCode: emptyToNull(values.conditionCode),
      diagnosisDate: values.diagnosisDate,
      recoveryDate: emptyToNull(values.recoveryDate),
      severity: values.severity,
      conditionStatus: values.conditionStatus,
      clinicalNotes: emptyToNull(values.clinicalNotes),
    };
    try {
      if (editingChronic) {
        await mutations.updateChronicCondition.mutateAsync({
          entryId: editingChronic.id,
          payload,
        });
        toast.success('Chronic condition updated');
      } else {
        await mutations.createChronicCondition.mutateAsync(payload);
        toast.success('Chronic condition added');
      }
      setDialogOpen(false);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to save chronic condition'));
    }
  });

  const onSubmitFamily = familyForm.handleSubmit(async (values) => {
    const payload = {
      diseaseName: values.diseaseName.trim(),
      diseaseCategory: values.diseaseCategory,
      diseaseCode: emptyToNull(values.diseaseCode),
      familyRelation: values.familyRelation,
      diagnosisDate: values.diagnosisDate,
      recoveryDate: emptyToNull(values.recoveryDate),
      severity: values.severity,
      conditionStatus: values.conditionStatus,
      clinicalNotes: emptyToNull(values.clinicalNotes),
    };
    try {
      if (editingFamily) {
        await mutations.updateFamilyHistory.mutateAsync({
          entryId: editingFamily.id,
          payload,
        });
        toast.success('Family history updated');
      } else {
        await mutations.createFamilyHistory.mutateAsync(payload);
        toast.success('Family history added');
      }
      setDialogOpen(false);
      setEditingFamily(null);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to save family history'));
    }
  });

  const onDelete = async () => {
    if (!deleting) {
      return;
    }
    try {
      if (deleting.kind === 'disease') {
        await mutations.deletePastDisease.mutateAsync(deleting.id);
      } else if (deleting.kind === 'surgery') {
        await mutations.deleteSurgery.mutateAsync(deleting.id);
      } else if (deleting.kind === 'chronic') {
        await mutations.deleteChronicCondition.mutateAsync(deleting.id);
      } else {
        await mutations.deleteFamilyHistory.mutateAsync(deleting.id);
      }
      toast.success('Entry removed');
      setDeleting(null);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to remove entry'));
    }
  };

  if (historyQuery.isError) {
    return (
      <EmptyState
        title="Unable to load medical history"
        description={getErrorMessage(historyQuery.error)}
      />
    );
  }

  if (historyQuery.isLoading || !historyQuery.data) {
    return <div className="text-muted-foreground py-10 text-center text-sm">Loading history…</div>;
  }

  const history = historyQuery.data;
  const familyHistories = history?.familyHistories ?? [];

  return (
    <div className="space-y-4">
      <Tabs defaultValue="diseases">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <TabsList variant="line" className="w-full justify-start overflow-x-auto sm:w-auto">
            <TabsTrigger value="diseases">Past diseases</TabsTrigger>
            <TabsTrigger value="surgeries">Surgeries</TabsTrigger>
            <TabsTrigger value="chronic">Chronic</TabsTrigger>
            <TabsTrigger value="family">Family</TabsTrigger>
          </TabsList>
          <Can permissions={[Permissions.PATIENT_UPDATE]}>
            <div className="flex flex-wrap gap-2">
              <Button
                type="button"
                size="sm"
                variant="outline"
                onClick={() => openCreate('disease')}
              >
                <PlusIcon data-icon="inline-start" />
                Disease
              </Button>
              <Button
                type="button"
                size="sm"
                variant="outline"
                onClick={() => openCreate('surgery')}
              >
                <PlusIcon data-icon="inline-start" />
                Surgery
              </Button>
              <Button
                type="button"
                size="sm"
                variant="outline"
                onClick={() => openCreate('chronic')}
              >
                <PlusIcon data-icon="inline-start" />
                Chronic
              </Button>
              <Button
                type="button"
                size="sm"
                variant="outline"
                onClick={() => openCreate('family')}
              >
                <PlusIcon data-icon="inline-start" />
                Family
              </Button>
            </div>
          </Can>
        </div>

        <TabsContent value="diseases" className="mt-4">
          <HistoryTable
            emptyTitle="No past diseases"
            rows={history.pastDiseases}
            nameKey="diseaseName"
            categoryKey="diseaseCategory"
            onEdit={(row) => {
              setKind('disease');
              setEditingDisease(row as PastDiseaseResponse);
              setEditingSurgery(null);
              setEditingChronic(null);
              setEditingFamily(null);
              setDialogOpen(true);
            }}
            onDelete={(row) =>
              setDeleting({
                kind: 'disease',
                id: row.id,
                label: (row as PastDiseaseResponse).diseaseName,
              })
            }
          />
        </TabsContent>
        <TabsContent value="surgeries" className="mt-4">
          <HistoryTable
            emptyTitle="No surgeries"
            rows={history.surgeries}
            nameKey="procedureName"
            categoryKey="procedureCategory"
            onEdit={(row) => {
              setKind('surgery');
              setEditingSurgery(row as SurgeryHistoryResponse);
              setEditingDisease(null);
              setEditingChronic(null);
              setEditingFamily(null);
              setDialogOpen(true);
            }}
            onDelete={(row) =>
              setDeleting({
                kind: 'surgery',
                id: row.id,
                label: (row as SurgeryHistoryResponse).procedureName,
              })
            }
          />
        </TabsContent>
        <TabsContent value="chronic" className="mt-4">
          <HistoryTable
            emptyTitle="No chronic conditions"
            rows={history.chronicConditions}
            nameKey="conditionName"
            categoryKey="diseaseCategory"
            onEdit={(row) => {
              setKind('chronic');
              setEditingChronic(row as ChronicConditionResponse);
              setEditingDisease(null);
              setEditingSurgery(null);
              setEditingFamily(null);
              setDialogOpen(true);
            }}
            onDelete={(row) =>
              setDeleting({
                kind: 'chronic',
                id: row.id,
                label: (row as ChronicConditionResponse).conditionName,
              })
            }
          />
        </TabsContent>
        <TabsContent value="family" className="mt-4">
          <HistoryTable
            emptyTitle="No family history"
            rows={familyHistories}
            nameKey="diseaseName"
            categoryKey="diseaseCategory"
            relationKey="familyRelation"
            onEdit={(row) => {
              setKind('family');
              setEditingFamily(row as FamilyHistoryResponse);
              setEditingDisease(null);
              setEditingSurgery(null);
              setEditingChronic(null);
              setDialogOpen(true);
            }}
            onDelete={(row) =>
              setDeleting({
                kind: 'family',
                id: row.id,
                label: (row as FamilyHistoryResponse).diseaseName,
              })
            }
          />
        </TabsContent>
      </Tabs>

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent className="sm:max-w-lg" showCloseButton>
          <DialogHeader>
            <DialogTitle>
              {kind === 'disease'
                ? editingDisease
                  ? 'Edit past disease'
                  : 'Add past disease'
                : kind === 'surgery'
                  ? editingSurgery
                    ? 'Edit surgery'
                    : 'Add surgery'
                  : kind === 'chronic'
                    ? editingChronic
                      ? 'Edit chronic condition'
                      : 'Add chronic condition'
                    : editingFamily
                      ? 'Edit family history'
                      : 'Add family history'}
            </DialogTitle>
            <DialogDescription>
              Structured clinical history supports searchable, auditable chart entries.
            </DialogDescription>
          </DialogHeader>

          {kind === 'disease' ? (
            <form className="grid gap-4" onSubmit={onSubmitDisease} noValidate>
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2 sm:col-span-2">
                  <Label htmlFor="disease-name">Disease name</Label>
                  <Input id="disease-name" {...diseaseForm.register('diseaseName')} />
                  <FieldError message={diseaseForm.formState.errors.diseaseName?.message} />
                </div>
                <EnumSelect
                  label="Category"
                  value={diseaseForm.watch('diseaseCategory')}
                  onValueChange={(value) =>
                    diseaseForm.setValue(
                      'diseaseCategory',
                      value as PastDiseaseFormValues['diseaseCategory'],
                      { shouldValidate: true },
                    )
                  }
                  options={DISEASE_CATEGORIES}
                />
                <div className="space-y-2">
                  <Label htmlFor="disease-code">Clinical code</Label>
                  <Input id="disease-code" {...diseaseForm.register('diseaseCode')} />
                </div>
                <SharedClinicalFields
                  form={diseaseForm as unknown as UseFormReturn<SharedClinicalValues>}
                  idPrefix="disease"
                />
              </div>
              <DialogFooter>
                <Button type="button" variant="outline" onClick={() => setDialogOpen(false)}>
                  Cancel
                </Button>
                <Button type="submit" disabled={isBusy}>
                  {isBusy ? (
                    <Loader2Icon className="animate-spin" data-icon="inline-start" />
                  ) : null}
                  Save
                </Button>
              </DialogFooter>
            </form>
          ) : null}

          {kind === 'surgery' ? (
            <form className="grid gap-4" onSubmit={onSubmitSurgery} noValidate>
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2 sm:col-span-2">
                  <Label htmlFor="procedure-name">Procedure name</Label>
                  <Input id="procedure-name" {...surgeryForm.register('procedureName')} />
                  <FieldError message={surgeryForm.formState.errors.procedureName?.message} />
                </div>
                <EnumSelect
                  label="Category"
                  value={surgeryForm.watch('procedureCategory')}
                  onValueChange={(value) =>
                    surgeryForm.setValue(
                      'procedureCategory',
                      value as SurgeryFormValues['procedureCategory'],
                      { shouldValidate: true },
                    )
                  }
                  options={PROCEDURE_CATEGORIES}
                />
                <div className="space-y-2">
                  <Label htmlFor="procedure-code">Clinical code</Label>
                  <Input id="procedure-code" {...surgeryForm.register('procedureCode')} />
                </div>
                <div className="space-y-2 sm:col-span-2">
                  <Label htmlFor="facility">Performing facility</Label>
                  <Input id="facility" {...surgeryForm.register('performingFacility')} />
                </div>
                <SharedClinicalFields
                  form={surgeryForm as unknown as UseFormReturn<SharedClinicalValues>}
                  idPrefix="surgery"
                />
              </div>
              <DialogFooter>
                <Button type="button" variant="outline" onClick={() => setDialogOpen(false)}>
                  Cancel
                </Button>
                <Button type="submit" disabled={isBusy}>
                  {isBusy ? (
                    <Loader2Icon className="animate-spin" data-icon="inline-start" />
                  ) : null}
                  Save
                </Button>
              </DialogFooter>
            </form>
          ) : null}

          {kind === 'chronic' ? (
            <form className="grid gap-4" onSubmit={onSubmitChronic} noValidate>
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2 sm:col-span-2">
                  <Label htmlFor="condition-name">Condition name</Label>
                  <Input id="condition-name" {...chronicForm.register('conditionName')} />
                  <FieldError message={chronicForm.formState.errors.conditionName?.message} />
                </div>
                <EnumSelect
                  label="Category"
                  value={chronicForm.watch('diseaseCategory')}
                  onValueChange={(value) =>
                    chronicForm.setValue(
                      'diseaseCategory',
                      value as ChronicConditionFormValues['diseaseCategory'],
                      { shouldValidate: true },
                    )
                  }
                  options={DISEASE_CATEGORIES}
                />
                <div className="space-y-2">
                  <Label htmlFor="condition-code">Clinical code</Label>
                  <Input id="condition-code" {...chronicForm.register('conditionCode')} />
                </div>
                <SharedClinicalFields
                  form={chronicForm as unknown as UseFormReturn<SharedClinicalValues>}
                  idPrefix="chronic"
                />
              </div>
              <DialogFooter>
                <Button type="button" variant="outline" onClick={() => setDialogOpen(false)}>
                  Cancel
                </Button>
                <Button type="submit" disabled={isBusy}>
                  {isBusy ? (
                    <Loader2Icon className="animate-spin" data-icon="inline-start" />
                  ) : null}
                  Save
                </Button>
              </DialogFooter>
            </form>
          ) : null}

          {kind === 'family' ? (
            <form className="grid gap-4" onSubmit={onSubmitFamily} noValidate>
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-2 sm:col-span-2">
                  <Label htmlFor="family-disease-name">Disease name</Label>
                  <Input id="family-disease-name" {...familyForm.register('diseaseName')} />
                  <FieldError message={familyForm.formState.errors.diseaseName?.message} />
                </div>
                <EnumSelect
                  label="Relation"
                  value={familyForm.watch('familyRelation')}
                  onValueChange={(value) =>
                    familyForm.setValue(
                      'familyRelation',
                      value as FamilyHistoryFormValues['familyRelation'],
                      { shouldValidate: true },
                    )
                  }
                  options={FAMILY_RELATIONS}
                />
                <EnumSelect
                  label="Category"
                  value={familyForm.watch('diseaseCategory')}
                  onValueChange={(value) =>
                    familyForm.setValue(
                      'diseaseCategory',
                      value as FamilyHistoryFormValues['diseaseCategory'],
                      { shouldValidate: true },
                    )
                  }
                  options={DISEASE_CATEGORIES}
                />
                <div className="space-y-2">
                  <Label htmlFor="family-disease-code">Clinical code</Label>
                  <Input id="family-disease-code" {...familyForm.register('diseaseCode')} />
                </div>
                <SharedClinicalFields
                  form={familyForm as unknown as UseFormReturn<SharedClinicalValues>}
                  idPrefix="family"
                />
              </div>
              <DialogFooter>
                <Button type="button" variant="outline" onClick={() => setDialogOpen(false)}>
                  Cancel
                </Button>
                <Button type="submit" disabled={isBusy}>
                  {isBusy ? (
                    <Loader2Icon className="animate-spin" data-icon="inline-start" />
                  ) : null}
                  Save
                </Button>
              </DialogFooter>
            </form>
          ) : null}
        </DialogContent>
      </Dialog>

      <Dialog open={Boolean(deleting)} onOpenChange={(open) => !open && setDeleting(null)}>
        <DialogContent showCloseButton>
          <DialogHeader>
            <DialogTitle>Remove history entry</DialogTitle>
            <DialogDescription>
              Soft-delete <span className="font-medium">{deleting?.label}</span>? The record remains
              recoverable for audit.
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

type HistoryRow =
  PastDiseaseResponse | SurgeryHistoryResponse | ChronicConditionResponse | FamilyHistoryResponse;

function HistoryTable({
  rows,
  emptyTitle,
  nameKey,
  categoryKey,
  relationKey,
  onEdit,
  onDelete,
}: {
  rows: HistoryRow[];
  emptyTitle: string;
  nameKey: 'diseaseName' | 'procedureName' | 'conditionName';
  categoryKey: 'diseaseCategory' | 'procedureCategory';
  relationKey?: 'familyRelation';
  onEdit: (row: HistoryRow) => void;
  onDelete: (row: HistoryRow) => void;
}) {
  if (rows.length === 0) {
    return (
      <EmptyState title={emptyTitle} description="Add structured entries to build the chart." />
    );
  }

  return (
    <div className="overflow-x-auto rounded-xl border">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Name</TableHead>
            {relationKey ? <TableHead>Relation</TableHead> : null}
            <TableHead>Category</TableHead>
            <TableHead>Date</TableHead>
            <TableHead>Severity</TableHead>
            <TableHead>Status</TableHead>
            <TableHead className="text-right">Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {rows.map((row) => {
            const name =
              nameKey === 'diseaseName'
                ? (row as PastDiseaseResponse | FamilyHistoryResponse).diseaseName
                : nameKey === 'procedureName'
                  ? (row as SurgeryHistoryResponse).procedureName
                  : (row as ChronicConditionResponse).conditionName;
            const category =
              categoryKey === 'procedureCategory'
                ? (row as SurgeryHistoryResponse).procedureCategory
                : (row as PastDiseaseResponse | ChronicConditionResponse | FamilyHistoryResponse)
                    .diseaseCategory;
            const relation = relationKey
              ? (row as FamilyHistoryResponse).familyRelation
              : undefined;

            return (
              <TableRow key={row.id}>
                <TableCell className="font-medium">{name}</TableCell>
                {relationKey ? <TableCell>{formatEnumLabel(relation ?? '')}</TableCell> : null}
                <TableCell>{formatEnumLabel(category)}</TableCell>
                <TableCell>{formatDate(row.diagnosisDate)}</TableCell>
                <TableCell>
                  <SeverityBadge severity={row.severity} />
                </TableCell>
                <TableCell>
                  <StatusBadge status={row.conditionStatus} />
                </TableCell>
                <TableCell className="text-right">
                  <div className="flex justify-end gap-1">
                    <Can permissions={[Permissions.PATIENT_UPDATE]}>
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon-sm"
                        aria-label={`Edit ${name}`}
                        onClick={() => onEdit(row)}
                      >
                        <PencilIcon />
                      </Button>
                    </Can>
                    <Can permissions={[Permissions.PATIENT_DELETE]}>
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon-sm"
                        aria-label={`Remove ${name}`}
                        onClick={() => onDelete(row)}
                      >
                        <Trash2Icon />
                      </Button>
                    </Can>
                  </div>
                </TableCell>
              </TableRow>
            );
          })}
        </TableBody>
      </Table>
    </div>
  );
}

type SharedClinicalValues = {
  diagnosisDate: string;
  recoveryDate: string;
  severity: PastDiseaseFormValues['severity'];
  conditionStatus: PastDiseaseFormValues['conditionStatus'];
  clinicalNotes: string;
};

function SharedClinicalFields({
  form,
  idPrefix,
}: {
  form: UseFormReturn<SharedClinicalValues>;
  idPrefix: string;
}) {
  return (
    <>
      <div className="space-y-2">
        <Label htmlFor={`${idPrefix}-date`}>Diagnosis / procedure date</Label>
        <Input id={`${idPrefix}-date`} type="date" {...form.register('diagnosisDate')} />
        <FieldError message={form.formState.errors.diagnosisDate?.message} />
      </div>
      <div className="space-y-2">
        <Label htmlFor={`${idPrefix}-recovery`}>Recovery date</Label>
        <Input id={`${idPrefix}-recovery`} type="date" {...form.register('recoveryDate')} />
        <FieldError message={form.formState.errors.recoveryDate?.message} />
      </div>
      <EnumSelect
        label="Severity"
        value={form.watch('severity')}
        onValueChange={(value) =>
          form.setValue('severity', value as SharedClinicalValues['severity'], {
            shouldValidate: true,
          })
        }
        options={CLINICAL_SEVERITIES}
      />
      <EnumSelect
        label="Condition status"
        value={form.watch('conditionStatus')}
        onValueChange={(value) =>
          form.setValue('conditionStatus', value as SharedClinicalValues['conditionStatus'], {
            shouldValidate: true,
          })
        }
        options={CLINICAL_CONDITION_STATUSES}
      />
      <div className="space-y-2 sm:col-span-2">
        <Label htmlFor={`${idPrefix}-notes`}>Clinical notes</Label>
        <Textarea id={`${idPrefix}-notes`} rows={3} {...form.register('clinicalNotes')} />
      </div>
    </>
  );
}
