'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2Icon } from 'lucide-react';
import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
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
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { useAppointmentMutations } from '@/features/appointments/hooks/use-appointments';
import {
  cancelAppointmentSchema,
  type CancelAppointmentFormValues,
} from '@/features/appointments/validation/appointment-schema';
import { getErrorMessage } from '@/lib/api-error';

type CancelAppointmentDialogProps = {
  appointmentId: string | null;
  appointmentNumber?: string;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onCancelled?: () => void;
};

export function CancelAppointmentDialog({
  appointmentId,
  appointmentNumber,
  open,
  onOpenChange,
  onCancelled,
}: CancelAppointmentDialogProps) {
  const mutations = useAppointmentMutations();
  const form = useForm<CancelAppointmentFormValues>({
    resolver: zodResolver(cancelAppointmentSchema),
    defaultValues: { reason: '' },
  });

  useEffect(() => {
    if (open) {
      form.reset({ reason: '' });
    }
  }, [open, form]);

  const onSubmit = form.handleSubmit(async (values) => {
    if (!appointmentId) {
      return;
    }
    try {
      await mutations.cancel.mutateAsync({
        id: appointmentId,
        payload: { reason: values.reason?.trim() || null },
      });
      toast.success('Appointment cancelled');
      onOpenChange(false);
      onCancelled?.();
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to cancel appointment'));
    }
  });

  const isBusy = mutations.cancel.isPending;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Cancel appointment</DialogTitle>
          <DialogDescription>
            {appointmentNumber
              ? `Cancel ${appointmentNumber}. This cannot be undone from the schedule.`
              : 'Cancel this appointment. This cannot be undone from the schedule.'}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={onSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="cancel-reason">Reason (optional)</Label>
            <Textarea id="cancel-reason" rows={3} {...form.register('reason')} />
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Keep appointment
            </Button>
            <Button type="submit" variant="destructive" disabled={isBusy}>
              {isBusy ? <Loader2Icon className="animate-spin" data-icon="inline-start" /> : null}
              Cancel appointment
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
