'use client';

import Link from 'next/link';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { EmptyState } from '@/features/hospital-admin/components/shared/empty-state';
import { AllergiesPanel } from '@/features/patients/components/allergies/allergies-panel';
import { AllergyBanner } from '@/features/patients/components/detail/allergy-banner';
import {
  DemographicsPanel,
  PatientChartHeader,
} from '@/features/patients/components/detail/patient-chart-header';
import { MedicalHistoryPanel } from '@/features/patients/components/history/medical-history-panel';
import { ImmunizationsPanel } from '@/features/patients/components/immunizations/immunizations-panel';
import { PatientPrescriptionsPanel } from '@/features/patients/components/prescriptions/patient-prescriptions-panel';
import { TimelinePanel } from '@/features/patients/components/timeline/timeline-panel';
import { usePatientQuery } from '@/features/patients/hooks/use-patients';
import {
  selectPatientDetailTab,
  setPatientDetailTab,
} from '@/features/patients/store/patients-ui-slice';
import { getErrorMessage } from '@/lib/api-error';
import { useAppDispatch, useAppSelector } from '@/store/hooks';

type PatientDetailPageProps = {
  patientId: string;
};

const DETAIL_TABS = [
  'overview',
  'history',
  'allergies',
  'vaccinations',
  'prescriptions',
  'timeline',
] as const;

export function PatientDetailPage({ patientId }: PatientDetailPageProps) {
  const dispatch = useAppDispatch();
  const tab = useAppSelector(selectPatientDetailTab);
  const patientQuery = usePatientQuery(patientId);

  if (patientQuery.isError) {
    return (
      <EmptyState
        title="Unable to load patient chart"
        description={getErrorMessage(patientQuery.error)}
        action={
          <Button nativeButton={false} variant="outline" render={<Link href="/app/patients" />}>
            Back to list
          </Button>
        }
      />
    );
  }

  if (patientQuery.isLoading || !patientQuery.data) {
    return (
      <div className="text-muted-foreground px-6 py-16 text-center text-sm">Loading chart…</div>
    );
  }

  const patient = patientQuery.data;
  const activeTab = DETAIL_TABS.includes(tab as (typeof DETAIL_TABS)[number]) ? tab : 'overview';

  return (
    <div className="mx-auto max-w-6xl space-y-6">
      <PatientChartHeader patient={patient} />
      <AllergyBanner patientId={patientId} />

      <Tabs
        value={activeTab}
        onValueChange={(value) => dispatch(setPatientDetailTab(value ?? 'overview'))}
      >
        <TabsList
          variant="line"
          className="h-auto w-full flex-wrap justify-start gap-1 overflow-x-auto"
          aria-label="Patient chart sections"
        >
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="history">Medical history</TabsTrigger>
          <TabsTrigger value="allergies">Allergies</TabsTrigger>
          <TabsTrigger value="vaccinations">Vaccinations</TabsTrigger>
          <TabsTrigger value="prescriptions">Prescriptions</TabsTrigger>
          <TabsTrigger value="timeline">Timeline</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="mt-4 space-y-4">
          <DemographicsPanel patient={patient} />
          <Card>
            <CardHeader>
              <CardTitle>Clinical chart</CardTitle>
            </CardHeader>
            <CardContent className="text-muted-foreground text-sm">
              Use the tabs above for structured medical history, allergy management, vaccinations,
              prescription history, and the chronological timeline. Allergy alerts remain visible at
              the top of every chart view.
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="history" className="mt-4">
          <Card>
            <CardHeader>
              <CardTitle>Medical history</CardTitle>
            </CardHeader>
            <CardContent>
              <MedicalHistoryPanel patientId={patientId} />
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="allergies" className="mt-4">
          <Card>
            <CardHeader>
              <CardTitle>Allergies</CardTitle>
            </CardHeader>
            <CardContent>
              <AllergiesPanel patientId={patientId} />
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="vaccinations" className="mt-4">
          <Card>
            <CardHeader>
              <CardTitle>Vaccinations</CardTitle>
            </CardHeader>
            <CardContent>
              <ImmunizationsPanel patientId={patientId} />
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="prescriptions" className="mt-4">
          <Card>
            <CardHeader>
              <CardTitle>Prescription history</CardTitle>
            </CardHeader>
            <CardContent>
              <PatientPrescriptionsPanel patientId={patientId} />
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="timeline" className="mt-4">
          <Card>
            <CardHeader>
              <CardTitle>Timeline</CardTitle>
            </CardHeader>
            <CardContent>
              <TimelinePanel patientId={patientId} />
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
