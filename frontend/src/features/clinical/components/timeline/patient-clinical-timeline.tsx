'use client';

import Link from 'next/link';

import { Button } from '@/components/ui/button';
import { Can } from '@/features/authorization/components/can';
import { Permissions } from '@/features/authorization/constants/permissions';
import { TimelinePanel } from '@/features/patients/components/timeline/timeline-panel';

type PatientClinicalTimelineProps = {
  patientId: string;
};

/**
 * Patient longitudinal timeline embedded in the consultation workspace.
 * Requires PATIENT_READ; falls back with a chart link when unavailable.
 */
export function PatientClinicalTimeline({ patientId }: PatientClinicalTimelineProps) {
  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-start justify-between gap-2">
        <div>
          <h2 className="text-base font-semibold">Patient timeline</h2>
          <p className="text-muted-foreground text-xs">
            Longitudinal history including follow-ups, allergies, immunizations, and prior events.
          </p>
        </div>
        <Can permissions={[Permissions.PATIENT_READ]}>
          <Button
            size="sm"
            variant="outline"
            nativeButton={false}
            render={<Link href={`/app/patients/${patientId}`} />}
          >
            Open full chart
          </Button>
        </Can>
      </div>

      <Can
        permissions={[Permissions.PATIENT_READ]}
        fallback={
          <p className="text-muted-foreground rounded-xl border border-dashed p-6 text-sm">
            Patient timeline requires patient read permission.
          </p>
        }
      >
        <TimelinePanel patientId={patientId} />
      </Can>
    </div>
  );
}
