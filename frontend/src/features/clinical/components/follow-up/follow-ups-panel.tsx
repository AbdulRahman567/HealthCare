'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2Icon, Trash2Icon } from 'lucide-react';
import { useForm } from 'react-hook-form';
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
  useConsultationFollowUpsQuery,
  useFollowUpMutations,
} from '@/features/clinical/hooks/use-follow-ups';
import { toFollowUpPayload } from '@/features/clinical/lib/clinical-format';
import { FOLLOW_UP_PRIORITIES, FOLLOW_UP_STATUSES } from '@/features/clinical/types/enums';
import {
  emptyFollowUpForm,
  followUpFormSchema,
  type FollowUpFormValues,
} from '@/features/clinical/validation/clinical-schemas';
import { EmptyState } from '@/features/hospital-admin/components/shared/empty-state';
import { StatusBadge } from '@/features/hospital-admin/components/shared/status-badge';
import { EnumSelect, FieldError } from '@/features/patients/components/shared/enum-select';
import { getErrorMessage } from '@/lib/api-error';
import { formatEnumLabel } from '@/lib/page-query';

type FollowUpsPanelProps = {
  consultationId: string;
  patientId: string;
  editable: boolean;
};

export function FollowUpsPanel({ consultationId, patientId, editable }: FollowUpsPanelProps) {
  const listQuery = useConsultationFollowUpsQuery(consultationId);
  const mutations = useFollowUpMutations(consultationId, patientId);
  const form = useForm<FollowUpFormValues>({
    resolver: zodResolver(followUpFormSchema),
    defaultValues: emptyFollowUpForm(),
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
    tab: 'follow-ups',
    watch,
    enabled: editable,
    onRestore: (values) => reset(values),
  });

  const onSubmit = handleSubmit(async (values) => {
    try {
      await mutations.create.mutateAsync(toFollowUpPayload(values));
      clearSavedDraft();
      reset(emptyFollowUpForm());
      toast.success('Follow-up planned');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to create follow-up'));
    }
  });

  const onStatus = async (id: string, nextStatus: FollowUpFormValues['status']) => {
    try {
      await mutations.updateStatus.mutateAsync({ id, payload: { status: nextStatus } });
      toast.success(`Follow-up marked ${formatEnumLabel(nextStatus)}`);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to update follow-up status'));
    }
  };

  const onDelete = async (id: string) => {
    try {
      await mutations.remove.mutateAsync(id);
      toast.success('Follow-up removed');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to delete follow-up'));
    }
  };

  const rows = listQuery.data ?? [];

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-base font-semibold">Follow-up plan</h2>
        <p className="text-muted-foreground text-xs">
          Schedule return visits with clinical recommendations and reminder preferences.
        </p>
      </div>

      <Can permissions={[Permissions.VISIT_UPDATE]}>
        {editable ? (
          <form onSubmit={onSubmit} noValidate className="space-y-4 rounded-xl border bg-card p-4">
            <div className="flex items-center justify-between gap-2">
              <h3 className="text-sm font-medium">Plan follow-up</h3>
              <DraftSaveIndicator status={status} lastSavedAt={lastSavedAt} />
            </div>
            <div className="grid gap-3 sm:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="fu-date">Scheduled date</Label>
                <Input id="fu-date" type="date" {...register('scheduledDate')} />
                <FieldError message={errors.scheduledDate?.message} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="fu-time">Scheduled time</Label>
                <Input id="fu-time" type="time" {...register('scheduledTime')} />
                <FieldError message={errors.scheduledTime?.message} />
              </div>
              <EnumSelect
                id="fu-status"
                label="Status"
                value={watch('status')}
                onValueChange={(value) =>
                  setValue('status', value as FollowUpFormValues['status'], {
                    shouldValidate: true,
                  })
                }
                options={FOLLOW_UP_STATUSES}
                error={errors.status?.message}
              />
              <EnumSelect
                id="fu-priority"
                label="Priority"
                value={watch('priority')}
                onValueChange={(value) =>
                  setValue('priority', value as FollowUpFormValues['priority'], {
                    shouldValidate: true,
                  })
                }
                options={FOLLOW_UP_PRIORITIES}
                error={errors.priority?.message}
              />
              <div className="space-y-2 sm:col-span-2">
                <Label htmlFor="fu-reason">Reason</Label>
                <Input id="fu-reason" {...register('reason')} />
                <FieldError message={errors.reason?.message} />
              </div>
              <div className="space-y-2 sm:col-span-2">
                <Label htmlFor="fu-rec">Clinical recommendations</Label>
                <Textarea id="fu-rec" rows={2} {...register('clinicalRecommendations')} />
                <FieldError message={errors.clinicalRecommendations?.message} />
              </div>
              <div className="space-y-2 sm:col-span-2">
                <Label htmlFor="fu-instructions">Patient instructions</Label>
                <Textarea id="fu-instructions" rows={2} {...register('instructions')} />
                <FieldError message={errors.instructions?.message} />
              </div>
              <label className="flex items-center gap-2 text-sm">
                <Checkbox
                  checked={watch('reminderEnabled')}
                  onCheckedChange={(checked) => setValue('reminderEnabled', checked === true)}
                />
                Enable reminder
              </label>
              <div className="space-y-2">
                <Label htmlFor="fu-lead">Reminder lead days</Label>
                <Input
                  id="fu-lead"
                  type="number"
                  min={0}
                  max={90}
                  {...register('reminderLeadDays')}
                />
                <FieldError message={errors.reminderLeadDays?.message} />
              </div>
            </div>
            <div className="flex justify-end">
              <Button type="submit" disabled={mutations.create.isPending}>
                {mutations.create.isPending ? (
                  <Loader2Icon className="animate-spin" data-icon="inline-start" />
                ) : null}
                Save follow-up
              </Button>
            </div>
          </form>
        ) : null}
      </Can>

      {listQuery.isError ? (
        <EmptyState
          title="Unable to load follow-ups"
          description={getErrorMessage(listQuery.error)}
        />
      ) : listQuery.isLoading ? (
        <div className="text-muted-foreground py-8 text-center text-sm">Loading follow-ups…</div>
      ) : rows.length === 0 ? (
        <EmptyState
          title="No follow-ups planned"
          description="Add a return visit plan when clinically indicated."
        />
      ) : (
        <div className="overflow-x-auto rounded-xl border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Date</TableHead>
                <TableHead>Priority</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Reason</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {rows.map((row) => (
                <TableRow key={row.id}>
                  <TableCell>
                    {row.scheduledDate}
                    {row.scheduledTime ? ` · ${row.scheduledTime}` : ''}
                  </TableCell>
                  <TableCell>
                    <StatusBadge status={row.priority} />
                  </TableCell>
                  <TableCell>
                    <StatusBadge status={row.status} />
                  </TableCell>
                  <TableCell className="max-w-xs truncate">{row.reason || '—'}</TableCell>
                  <TableCell className="text-right">
                    <div className="flex flex-wrap justify-end gap-1">
                      <Can permissions={[Permissions.VISIT_UPDATE]}>
                        {row.status === 'PENDING' || row.status === 'SCHEDULED' ? (
                          <Button
                            type="button"
                            size="sm"
                            variant="outline"
                            onClick={() => void onStatus(row.id, 'COMPLETED')}
                          >
                            Complete
                          </Button>
                        ) : null}
                      </Can>
                      <Can permissions={[Permissions.VISIT_DELETE]}>
                        {editable ? (
                          <Button
                            type="button"
                            size="sm"
                            variant="ghost"
                            onClick={() => void onDelete(row.id)}
                          >
                            <Trash2Icon data-icon="inline-start" />
                            Delete
                          </Button>
                        ) : null}
                      </Can>
                    </div>
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
