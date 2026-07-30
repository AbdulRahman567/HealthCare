'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2Icon, PaperclipIcon, Trash2Icon } from 'lucide-react';
import { useRef } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Can } from '@/features/authorization/components/can';
import { Permissions } from '@/features/authorization/constants/permissions';
import { DraftSaveIndicator } from '@/features/clinical/components/shared/draft-save-indicator';
import {
  useClinicalNoteMutations,
  useConsultationNotesQuery,
} from '@/features/clinical/hooks/use-clinical-notes';
import { useAutoSaveDraft } from '@/features/clinical/hooks/use-auto-save-draft';
import { formatInstant, toClinicalNotePayload } from '@/features/clinical/lib/clinical-format';
import { CLINICAL_NOTE_TYPES } from '@/features/clinical/types/enums';
import {
  clinicalNoteFormSchema,
  emptyClinicalNoteForm,
  type ClinicalNoteFormValues,
} from '@/features/clinical/validation/clinical-schemas';
import { EmptyState } from '@/features/hospital-admin/components/shared/empty-state';
import { StatusBadge } from '@/features/hospital-admin/components/shared/status-badge';
import { EnumSelect, FieldError } from '@/features/patients/components/shared/enum-select';
import { getErrorMessage } from '@/lib/api-error';
import { formatEnumLabel } from '@/lib/page-query';

type ClinicalNotesPanelProps = {
  consultationId: string;
  patientId: string;
  editable: boolean;
};

export function ClinicalNotesPanel({
  consultationId,
  patientId,
  editable,
}: ClinicalNotesPanelProps) {
  const listQuery = useConsultationNotesQuery(consultationId);
  const mutations = useClinicalNoteMutations(consultationId, patientId);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const pendingNoteIdRef = useRef<string | null>(null);

  const form = useForm<ClinicalNoteFormValues>({
    resolver: zodResolver(clinicalNoteFormSchema),
    defaultValues: emptyClinicalNoteForm(),
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
    tab: 'notes',
    watch,
    enabled: editable,
    onRestore: (values) => reset(values),
  });

  const onSubmit = handleSubmit(async (values) => {
    try {
      await mutations.create.mutateAsync(toClinicalNotePayload(values));
      clearSavedDraft();
      reset(emptyClinicalNoteForm());
      toast.success('Clinical note saved');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to save note'));
    }
  });

  const onDelete = async (noteId: string) => {
    try {
      await mutations.remove.mutateAsync(noteId);
      toast.success('Note deleted');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to delete note'));
    }
  };

  const onAttachClick = (noteId: string) => {
    pendingNoteIdRef.current = noteId;
    fileInputRef.current?.click();
  };

  const onFileSelected = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    const noteId = pendingNoteIdRef.current;
    event.target.value = '';
    if (!file || !noteId) {
      return;
    }
    try {
      await mutations.uploadAttachment.mutateAsync({ noteId, file });
      toast.success('Attachment uploaded');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to upload attachment'));
    }
  };

  const rows = listQuery.data ?? [];

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-base font-semibold">Clinical notes</h2>
        <p className="text-muted-foreground text-xs">
          SOAP and progress documentation with optional image/PDF attachments.
        </p>
      </div>

      <input
        ref={fileInputRef}
        type="file"
        className="hidden"
        accept="image/jpeg,image/png,image/webp,application/pdf"
        onChange={(event) => void onFileSelected(event)}
      />

      <Can permissions={[Permissions.VISIT_UPDATE]}>
        {editable ? (
          <form onSubmit={onSubmit} noValidate className="space-y-4 rounded-xl border bg-card p-4">
            <div className="flex items-center justify-between gap-2">
              <h3 className="text-sm font-medium">Add note</h3>
              <DraftSaveIndicator status={status} lastSavedAt={lastSavedAt} />
            </div>
            <div className="grid gap-3 sm:grid-cols-2">
              <EnumSelect
                id="note-type"
                label="Note type"
                value={watch('noteType')}
                onValueChange={(value) =>
                  setValue('noteType', value as ClinicalNoteFormValues['noteType'], {
                    shouldValidate: true,
                  })
                }
                options={CLINICAL_NOTE_TYPES}
                error={errors.noteType?.message}
              />
              <div className="space-y-2">
                <Label htmlFor="note-title">Title</Label>
                <Input id="note-title" {...register('title')} />
                <FieldError message={errors.title?.message} />
              </div>
              <div className="space-y-2 sm:col-span-2">
                <Label htmlFor="note-content">Content</Label>
                <Textarea id="note-content" rows={5} {...register('content')} />
                <FieldError message={errors.content?.message} />
              </div>
            </div>
            <div className="flex justify-end">
              <Button type="submit" disabled={mutations.create.isPending}>
                {mutations.create.isPending ? (
                  <Loader2Icon className="animate-spin" data-icon="inline-start" />
                ) : null}
                Save note
              </Button>
            </div>
          </form>
        ) : null}
      </Can>

      {listQuery.isError ? (
        <EmptyState title="Unable to load notes" description={getErrorMessage(listQuery.error)} />
      ) : listQuery.isLoading ? (
        <div className="text-muted-foreground py-8 text-center text-sm">Loading notes…</div>
      ) : rows.length === 0 ? (
        <EmptyState
          title="No clinical notes yet"
          description="Capture SOAP, progress, or procedure notes for this encounter."
        />
      ) : (
        <ul className="space-y-3">
          {rows.map((note) => (
            <li key={note.id} className="rounded-xl border p-4">
              <div className="mb-2 flex flex-wrap items-start justify-between gap-2">
                <div className="space-y-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <h3 className="font-medium">{note.title || formatEnumLabel(note.noteType)}</h3>
                    <StatusBadge status={note.noteType} />
                  </div>
                  <p className="text-muted-foreground text-xs">
                    {note.authorDoctorName} · {formatInstant(note.recordedAt)}
                  </p>
                </div>
                <div className="flex flex-wrap gap-2">
                  <Can permissions={[Permissions.VISIT_UPDATE]}>
                    {editable ? (
                      <Button
                        type="button"
                        size="sm"
                        variant="outline"
                        onClick={() => onAttachClick(note.id)}
                        disabled={mutations.uploadAttachment.isPending}
                      >
                        <PaperclipIcon data-icon="inline-start" />
                        Attach
                      </Button>
                    ) : null}
                  </Can>
                  <Can permissions={[Permissions.VISIT_DELETE]}>
                    {editable ? (
                      <Button
                        type="button"
                        size="sm"
                        variant="ghost"
                        onClick={() => void onDelete(note.id)}
                        disabled={mutations.remove.isPending}
                      >
                        <Trash2Icon data-icon="inline-start" />
                        Delete
                      </Button>
                    ) : null}
                  </Can>
                </div>
              </div>
              <p className="text-sm whitespace-pre-wrap text-pretty">{note.content}</p>
              {note.attachments?.length ? (
                <ul className="mt-3 space-y-1 border-t pt-3">
                  {note.attachments.map((attachment) => (
                    <li
                      key={attachment.id}
                      className="text-muted-foreground flex items-center gap-2 text-xs"
                    >
                      <PaperclipIcon className="size-3.5" aria-hidden />
                      {attachment.fileName} ({Math.round(attachment.sizeBytes / 1024)} KB)
                    </li>
                  ))}
                </ul>
              ) : null}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
