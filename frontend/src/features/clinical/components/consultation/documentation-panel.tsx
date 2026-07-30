'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2Icon, SaveIcon } from 'lucide-react';
import { useCallback, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Can } from '@/features/authorization/components/can';
import { Permissions } from '@/features/authorization/constants/permissions';
import { DraftSaveIndicator } from '@/features/clinical/components/shared/draft-save-indicator';
import { useAutoSaveDraft } from '@/features/clinical/hooks/use-auto-save-draft';
import { useConsultationMutations } from '@/features/clinical/hooks/use-consultations';
import {
  documentationFromSummary,
  toDocumentationPayload,
} from '@/features/clinical/lib/clinical-format';
import type { ConsultationResponse } from '@/features/clinical/types/consultation';
import {
  documentationSchema,
  type DocumentationFormValues,
} from '@/features/clinical/validation/clinical-schemas';
import { FieldError } from '@/features/patients/components/shared/enum-select';
import { getErrorMessage } from '@/lib/api-error';

type DocumentationPanelProps = {
  consultation: ConsultationResponse;
  editable: boolean;
};

export function DocumentationPanel({ consultation, editable }: DocumentationPanelProps) {
  const mutations = useConsultationMutations();
  const form = useForm<DocumentationFormValues>({
    resolver: zodResolver(documentationSchema),
    defaultValues: documentationFromSummary(consultation.clinicalSummary),
  });

  const {
    register,
    reset,
    handleSubmit,
    watch,
    formState: { errors, isDirty },
  } = form;

  useEffect(() => {
    reset(documentationFromSummary(consultation.clinicalSummary));
  }, [consultation.clinicalSummary, consultation.updatedAt, reset]);

  const onRestore = useCallback(
    (values: DocumentationFormValues) => {
      reset(values, { keepDefaultValues: false });
    },
    [reset],
  );

  const { status, lastSavedAt, clearSavedDraft } = useAutoSaveDraft({
    consultationId: consultation.id,
    tab: 'chart',
    watch,
    enabled: editable,
    onRestore,
  });

  const save = handleSubmit(async (values) => {
    try {
      await mutations.updateDocumentation.mutateAsync({
        id: consultation.id,
        payload: toDocumentationPayload(values),
      });
      clearSavedDraft();
      toast.success('Chart documentation saved');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to save documentation'));
    }
  });

  useEffect(() => {
    if (!editable) {
      return;
    }
    const onKeyDown = (event: KeyboardEvent) => {
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 's') {
        event.preventDefault();
        void save();
      }
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [editable, save]);

  const busy = mutations.updateDocumentation.isPending;

  return (
    <form onSubmit={save} noValidate className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div>
          <h2 className="text-base font-semibold">Clinical chart</h2>
          <p className="text-muted-foreground text-xs">
            Chief complaint, HPI, exam, and advice. Auto-saves a local draft while you type.
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-3">
          {editable ? <DraftSaveIndicator status={status} lastSavedAt={lastSavedAt} /> : null}
          <Can permissions={[Permissions.VISIT_UPDATE]}>
            <Button type="submit" size="sm" disabled={!editable || busy || !isDirty}>
              {busy ? (
                <Loader2Icon className="animate-spin" data-icon="inline-start" />
              ) : (
                <SaveIcon data-icon="inline-start" />
              )}
              Save chart
            </Button>
          </Can>
        </div>
      </div>

      <div className="grid gap-4">
        {(
          [
            ['chiefComplaint', 'Chief complaint', 3],
            ['historyOfPresentIllness', 'History of present illness', 5],
            ['physicalExamination', 'Physical examination', 5],
            ['doctorNotes', 'Doctor notes', 4],
            ['summary', 'Summary', 3],
            ['advice', 'Advice', 3],
          ] as const
        ).map(([name, label, rows]) => (
          <div key={name} className="space-y-2">
            <Label htmlFor={`doc-${name}`}>{label}</Label>
            <Textarea id={`doc-${name}`} rows={rows} disabled={!editable} {...register(name)} />
            <FieldError message={errors[name]?.message} />
          </div>
        ))}
      </div>
    </form>
  );
}
