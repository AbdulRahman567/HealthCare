'use client';

import { AnimatePresence, motion } from 'framer-motion';
import {
  CheckIcon,
  Loader2Icon,
  PauseIcon,
  PlayIcon,
  RotateCcwIcon,
  XIcon,
} from 'lucide-react';
import Link from 'next/link';
import { useEffect, useState } from 'react';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Textarea } from '@/components/ui/textarea';
import { Can } from '@/features/authorization/components/can';
import { Permissions } from '@/features/authorization/constants/permissions';
import { DocumentationPanel } from '@/features/clinical/components/consultation/documentation-panel';
import { DiagnosisPanel } from '@/features/clinical/components/diagnosis/diagnosis-panel';
import { FollowUpsPanel } from '@/features/clinical/components/follow-up/follow-ups-panel';
import { ClinicalNotesPanel } from '@/features/clinical/components/notes/clinical-notes-panel';
import { PrescriptionsPanel } from '@/features/clinical/components/prescriptions/prescriptions-panel';
import { KeyboardShortcutsHint } from '@/features/clinical/components/shared/draft-save-indicator';
import { PatientClinicalTimeline } from '@/features/clinical/components/timeline/patient-clinical-timeline';
import { VitalsPanel } from '@/features/clinical/components/vitals/vitals-panel';
import {
  useConsultationMutations,
  useConsultationQuery,
} from '@/features/clinical/hooks/use-consultations';
import { isEditableConsultationStatus } from '@/features/clinical/lib/clinical-format';
import {
  selectClinicalWorkspaceTab,
  setClinicalWorkspaceTab,
} from '@/features/clinical/store/clinical-ui-slice';
import {
  CONSULTATION_WORKSPACE_TABS,
  type ConsultationWorkspaceTab,
} from '@/features/clinical/types/enums';
import { AdminPageHeader } from '@/features/hospital-admin/components/shared/admin-page-header';
import { EmptyState } from '@/features/hospital-admin/components/shared/empty-state';
import { StatusBadge } from '@/features/hospital-admin/components/shared/status-badge';
import { AllergyBanner } from '@/features/patients/components/detail/allergy-banner';
import { getErrorMessage } from '@/lib/api-error';
import { useAppDispatch, useAppSelector } from '@/store/hooks';

type ConsultationWorkspacePageProps = {
  consultationId: string;
};

const TAB_LABELS: Record<ConsultationWorkspaceTab, string> = {
  chart: 'Chart',
  vitals: 'Vitals',
  diagnosis: 'Diagnosis',
  prescriptions: 'Rx',
  notes: 'Notes',
  'follow-ups': 'Follow-up',
  timeline: 'Timeline',
};

export function ConsultationWorkspacePage({ consultationId }: ConsultationWorkspacePageProps) {
  const dispatch = useAppDispatch();
  const tab = useAppSelector(selectClinicalWorkspaceTab);
  const consultationQuery = useConsultationQuery(consultationId);
  const mutations = useConsultationMutations();
  const [completeOpen, setCompleteOpen] = useState(false);
  const [cancelOpen, setCancelOpen] = useState(false);
  const [completeSummary, setCompleteSummary] = useState('');
  const [completeAdvice, setCompleteAdvice] = useState('');

  const activeTab = CONSULTATION_WORKSPACE_TABS.includes(tab) ? tab : 'chart';

  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      const target = event.target as HTMLElement | null;
      const typing =
        target?.tagName === 'INPUT' ||
        target?.tagName === 'TEXTAREA' ||
        target?.isContentEditable;
      if (typing || event.ctrlKey || event.metaKey || event.altKey) {
        return;
      }
      const index = Number(event.key);
      if (index >= 1 && index <= CONSULTATION_WORKSPACE_TABS.length) {
        event.preventDefault();
        dispatch(setClinicalWorkspaceTab(CONSULTATION_WORKSPACE_TABS[index - 1]));
      }
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [dispatch]);

  if (consultationQuery.isError) {
    return (
      <div className="mx-auto max-w-4xl space-y-6">
        <EmptyState
          title="Consultation not found"
          description={getErrorMessage(consultationQuery.error)}
          action={
            <Button nativeButton={false} variant="outline" render={<Link href="/app/clinical" />}>
              Back to list
            </Button>
          }
        />
      </div>
    );
  }

  if (consultationQuery.isLoading || !consultationQuery.data) {
    return (
      <div className="text-muted-foreground mx-auto max-w-4xl py-16 text-center text-sm">
        Loading consultation…
      </div>
    );
  }

  const consultation = consultationQuery.data;
  const editable = isEditableConsultationStatus(consultation.status);
  const lifecycleBusy =
    mutations.start.isPending ||
    mutations.pause.isPending ||
    mutations.resume.isPending ||
    mutations.complete.isPending ||
    mutations.cancel.isPending;

  const runLifecycle = async (
    action: 'start' | 'pause' | 'resume',
    successMessage: string,
  ) => {
    try {
      await mutations[action].mutateAsync(consultation.id);
      toast.success(successMessage);
    } catch (error) {
      toast.error(getErrorMessage(error, `Unable to ${action} consultation`));
    }
  };

  const onComplete = async () => {
    try {
      await mutations.complete.mutateAsync({
        id: consultation.id,
        payload: {
          summary: completeSummary.trim() || null,
          advice: completeAdvice.trim() || null,
        },
      });
      setCompleteOpen(false);
      toast.success('Consultation completed');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to complete consultation'));
    }
  };

  const onCancel = async () => {
    try {
      await mutations.cancel.mutateAsync(consultation.id);
      setCancelOpen(false);
      toast.success('Consultation cancelled');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to cancel consultation'));
    }
  };

  return (
    <div className="mx-auto max-w-7xl space-y-4">
      <AdminPageHeader
        title={consultation.consultationNumber}
        description={`${consultation.patientName} · ${consultation.patientMrn} · ${consultation.doctorName} · ${consultation.departmentName}`}
        actions={
          <div className="flex flex-wrap gap-2">
            <Button nativeButton={false} variant="outline" render={<Link href="/app/clinical" />}>
              Back
            </Button>
            <Can permissions={[Permissions.PATIENT_READ]}>
              <Button
                nativeButton={false}
                variant="outline"
                render={<Link href={`/app/patients/${consultation.patientId}`} />}
              >
                Patient chart
              </Button>
            </Can>
            <Can permissions={[Permissions.VISIT_UPDATE]}>
              {consultation.status === 'DRAFT' ? (
                <Button
                  type="button"
                  disabled={lifecycleBusy}
                  onClick={() => void runLifecycle('start', 'Consultation started')}
                >
                  {lifecycleBusy ? (
                    <Loader2Icon className="animate-spin" data-icon="inline-start" />
                  ) : (
                    <PlayIcon data-icon="inline-start" />
                  )}
                  Start
                </Button>
              ) : null}
              {consultation.status === 'IN_PROGRESS' ? (
                <Button
                  type="button"
                  variant="outline"
                  disabled={lifecycleBusy}
                  onClick={() => void runLifecycle('pause', 'Consultation paused')}
                >
                  <PauseIcon data-icon="inline-start" />
                  Pause
                </Button>
              ) : null}
              {consultation.status === 'PAUSED' ? (
                <Button
                  type="button"
                  disabled={lifecycleBusy}
                  onClick={() => void runLifecycle('resume', 'Consultation resumed')}
                >
                  <RotateCcwIcon data-icon="inline-start" />
                  Resume
                </Button>
              ) : null}
              {consultation.status === 'IN_PROGRESS' || consultation.status === 'PAUSED' ? (
                <Button type="button" disabled={lifecycleBusy} onClick={() => setCompleteOpen(true)}>
                  <CheckIcon data-icon="inline-start" />
                  Complete
                </Button>
              ) : null}
              {editable ? (
                <Button
                  type="button"
                  variant="outline"
                  disabled={lifecycleBusy}
                  onClick={() => setCancelOpen(true)}
                >
                  <XIcon data-icon="inline-start" />
                  Cancel
                </Button>
              ) : null}
            </Can>
          </div>
        }
      />

      <div className="flex flex-wrap items-center justify-between gap-3">
        <StatusBadge status={consultation.status} />
        <KeyboardShortcutsHint />
      </div>

      <Can permissions={[Permissions.PATIENT_READ]}>
        <AllergyBanner patientId={consultation.patientId} />
      </Can>

      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-base">Encounter summary</CardTitle>
        </CardHeader>
        <CardContent className="grid gap-3 text-sm sm:grid-cols-2 lg:grid-cols-4">
          <Detail label="Date" value={consultation.consultationDate} />
          <Detail label="Chief complaint" value={consultation.clinicalSummary.chiefComplaint} />
          <Detail label="Appointment" value={consultation.appointmentId ?? '—'} />
          <Detail
            label="Editable"
            value={editable ? 'Yes — chart open' : 'No — closed / cancelled'}
          />
        </CardContent>
      </Card>

      <Tabs
        value={activeTab}
        onValueChange={(value) =>
          dispatch(setClinicalWorkspaceTab((value ?? 'chart') as ConsultationWorkspaceTab))
        }
      >
        <TabsList
          variant="line"
          className="h-auto w-full flex-wrap justify-start gap-1 overflow-x-auto"
          aria-label="Consultation sections"
        >
          {CONSULTATION_WORKSPACE_TABS.map((key, index) => (
            <TabsTrigger key={key} value={key} className="gap-1">
              <span className="text-muted-foreground hidden text-[10px] sm:inline">
                {index + 1}
              </span>
              {TAB_LABELS[key]}
            </TabsTrigger>
          ))}
        </TabsList>

        <AnimatePresence mode="wait">
          <motion.div
            key={activeTab}
            initial={{ opacity: 0, y: 6 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -4 }}
            transition={{ duration: 0.18 }}
          >
            <TabsContent value="chart" className="mt-4">
              <Card>
                <CardContent className="pt-6">
                  <DocumentationPanel consultation={consultation} editable={editable} />
                </CardContent>
              </Card>
            </TabsContent>
            <TabsContent value="vitals" className="mt-4">
              <Card>
                <CardContent className="pt-6">
                  <VitalsPanel
                    consultationId={consultation.id}
                    patientId={consultation.patientId}
                    editable={editable}
                  />
                </CardContent>
              </Card>
            </TabsContent>
            <TabsContent value="diagnosis" className="mt-4">
              <Card>
                <CardContent className="pt-6">
                  <DiagnosisPanel
                    consultationId={consultation.id}
                    patientId={consultation.patientId}
                    editable={editable}
                  />
                </CardContent>
              </Card>
            </TabsContent>
            <TabsContent value="prescriptions" className="mt-4">
              <Card>
                <CardContent className="pt-6">
                  <Can
                    permissions={[Permissions.PRESCRIPTION_READ]}
                    fallback={
                      <p className="text-muted-foreground text-sm">
                        Prescription access requires prescription read permission.
                      </p>
                    }
                  >
                    <PrescriptionsPanel
                      consultationId={consultation.id}
                      patientId={consultation.patientId}
                      editable={editable}
                    />
                  </Can>
                </CardContent>
              </Card>
            </TabsContent>
            <TabsContent value="notes" className="mt-4">
              <Card>
                <CardContent className="pt-6">
                  <ClinicalNotesPanel
                    consultationId={consultation.id}
                    patientId={consultation.patientId}
                    editable={editable}
                  />
                </CardContent>
              </Card>
            </TabsContent>
            <TabsContent value="follow-ups" className="mt-4">
              <Card>
                <CardContent className="pt-6">
                  <FollowUpsPanel
                    consultationId={consultation.id}
                    patientId={consultation.patientId}
                    editable={editable}
                  />
                </CardContent>
              </Card>
            </TabsContent>
            <TabsContent value="timeline" className="mt-4">
              <Card>
                <CardContent className="pt-6">
                  <PatientClinicalTimeline patientId={consultation.patientId} />
                </CardContent>
              </Card>
            </TabsContent>
          </motion.div>
        </AnimatePresence>
      </Tabs>

      <Dialog open={completeOpen} onOpenChange={setCompleteOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Complete consultation</DialogTitle>
            <DialogDescription>
              Closing the chart makes clinical documentation read-only. Optionally set final
              summary and advice.
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-3">
            <div className="space-y-2">
              <Label htmlFor="complete-summary">Summary</Label>
              <Textarea
                id="complete-summary"
                rows={3}
                value={completeSummary}
                onChange={(event) => setCompleteSummary(event.target.value)}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="complete-advice">Advice</Label>
              <Textarea
                id="complete-advice"
                rows={3}
                value={completeAdvice}
                onChange={(event) => setCompleteAdvice(event.target.value)}
              />
            </div>
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => setCompleteOpen(false)}>
              Cancel
            </Button>
            <Button type="button" disabled={mutations.complete.isPending} onClick={() => void onComplete()}>
              {mutations.complete.isPending ? (
                <Loader2Icon className="animate-spin" data-icon="inline-start" />
              ) : null}
              Complete
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={cancelOpen} onOpenChange={setCancelOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Cancel consultation</DialogTitle>
            <DialogDescription>
              Cancel this encounter? The chart becomes read-only. A linked queue entry in
              consultation will be released. The appointment is not completed.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => setCancelOpen(false)}>
              Keep open
            </Button>
            <Button
              type="button"
              variant="destructive"
              disabled={mutations.cancel.isPending}
              onClick={() => void onCancel()}
            >
              {mutations.cancel.isPending ? (
                <Loader2Icon className="animate-spin" data-icon="inline-start" />
              ) : null}
              Cancel consultation
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

function Detail({ label, value }: { label: string; value: string | null | undefined }) {
  return (
    <div>
      <p className="text-muted-foreground text-xs">{label}</p>
      <p className="font-medium text-pretty">{value?.trim() || '—'}</p>
    </div>
  );
}
