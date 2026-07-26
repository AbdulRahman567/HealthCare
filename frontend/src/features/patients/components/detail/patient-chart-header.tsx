'use client';

import { Loader2Icon, PencilIcon, UserRoundCheckIcon, UserRoundXIcon } from 'lucide-react';
import Link from 'next/link';
import { useState } from 'react';
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
import { Can } from '@/features/authorization/components/can';
import { Permissions } from '@/features/authorization/constants/permissions';
import { StatusBadge } from '@/features/hospital-admin/components/shared/status-badge';
import { usePatientMutations } from '@/features/patients/hooks/use-patients';
import {
  calculateAge,
  formatBloodGroup,
  formatDate,
  patientDisplayName,
} from '@/features/patients/lib/patient-format';
import type { PatientResponse } from '@/features/patients/types/patient';
import { getErrorMessage } from '@/lib/api-error';
import { formatEnumLabel } from '@/lib/page-query';

type PatientChartHeaderProps = {
  patient: PatientResponse;
};

export function PatientChartHeader({ patient }: PatientChartHeaderProps) {
  const mutations = usePatientMutations();
  const [confirmDeactivate, setConfirmDeactivate] = useState(false);
  const age = calculateAge(patient.dateOfBirth);
  const isBusy = mutations.deactivate.isPending || mutations.reactivate.isPending;

  const onDeactivate = async () => {
    try {
      await mutations.deactivate.mutateAsync(patient.id);
      toast.success('Patient deactivated');
      setConfirmDeactivate(false);
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to deactivate patient'));
    }
  };

  const onReactivate = async () => {
    try {
      await mutations.reactivate.mutateAsync(patient.id);
      toast.success('Patient reactivated');
    } catch (error) {
      toast.error(getErrorMessage(error, 'Unable to reactivate patient'));
    }
  };

  return (
    <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
      <div className="space-y-2">
        <div className="flex flex-wrap items-center gap-2">
          <h1 className="text-2xl font-semibold tracking-tight">{patientDisplayName(patient)}</h1>
          <StatusBadge status={patient.status} />
        </div>
        <dl className="text-muted-foreground flex flex-wrap gap-x-4 gap-y-1 text-sm">
          <div>
            <dt className="sr-only">MRN</dt>
            <dd>
              <span className="text-foreground font-mono text-xs font-medium">{patient.mrn}</span>
            </dd>
          </div>
          <div>
            <dt className="sr-only">Date of birth</dt>
            <dd>
              {formatDate(patient.dateOfBirth)}
              {age !== null ? ` · ${age} yrs` : ''}
            </dd>
          </div>
          <div>
            <dt className="sr-only">Gender</dt>
            <dd>{formatEnumLabel(patient.gender)}</dd>
          </div>
          <div>
            <dt className="sr-only">Blood group</dt>
            <dd>{formatBloodGroup(patient.bloodGroup)}</dd>
          </div>
        </dl>
      </div>

      <div className="flex flex-wrap items-center gap-2">
        <Can permissions={[Permissions.PATIENT_UPDATE]}>
          <Button
            nativeButton={false}
            variant="outline"
            render={<Link href={`/app/patients/${patient.id}/edit`} />}
          >
            <PencilIcon data-icon="inline-start" />
            Edit
          </Button>
          {patient.status === 'ACTIVE' ? (
            <Button
              type="button"
              variant="outline"
              disabled={isBusy}
              onClick={() => setConfirmDeactivate(true)}
            >
              <UserRoundXIcon data-icon="inline-start" />
              Deactivate
            </Button>
          ) : null}
          {patient.status === 'INACTIVE' ? (
            <Button type="button" variant="outline" disabled={isBusy} onClick={onReactivate}>
              {isBusy ? (
                <Loader2Icon className="animate-spin" data-icon="inline-start" />
              ) : (
                <UserRoundCheckIcon data-icon="inline-start" />
              )}
              Reactivate
            </Button>
          ) : null}
        </Can>
        <Button nativeButton={false} variant="ghost" render={<Link href="/app/patients" />}>
          Back to list
        </Button>
      </div>

      <Dialog open={confirmDeactivate} onOpenChange={setConfirmDeactivate}>
        <DialogContent showCloseButton>
          <DialogHeader>
            <DialogTitle>Deactivate patient</DialogTitle>
            <DialogDescription>
              Set <span className="font-medium">{patientDisplayName(patient)}</span> (MRN{' '}
              <span className="font-mono text-xs">{patient.mrn}</span>) to inactive? Clinical chart
              writes will be blocked until reactivation.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => setConfirmDeactivate(false)}>
              Cancel
            </Button>
            <Button type="button" variant="destructive" disabled={isBusy} onClick={onDeactivate}>
              {isBusy ? <Loader2Icon className="animate-spin" data-icon="inline-start" /> : null}
              Deactivate
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

type DemographicsPanelProps = {
  patient: PatientResponse;
};

export function DemographicsPanel({ patient }: DemographicsPanelProps) {
  const rows: Array<{ label: string; value: string }> = [
    { label: 'MRN', value: patient.mrn },
    { label: 'National ID', value: patient.nationalId || '—' },
    { label: 'Phone', value: patient.phone || '—' },
    { label: 'Email', value: patient.email || '—' },
    { label: 'Marital status', value: patient.maritalStatus ? formatEnumLabel(patient.maritalStatus) : '—' },
    { label: 'Address', value: patient.address || '—' },
    {
      label: 'Emergency contact',
      value: patient.emergencyContact
        ? `${patient.emergencyContact.name} · ${patient.emergencyContact.phone}${
            patient.emergencyContact.relation ? ` · ${patient.emergencyContact.relation}` : ''
          }`
        : '—',
    },
  ];

  return (
    <Card>
      <CardHeader>
        <CardTitle>Demographics</CardTitle>
      </CardHeader>
      <CardContent>
        <dl className="grid gap-3 sm:grid-cols-2">
          {rows.map((row) => (
            <div key={row.label} className="space-y-1">
              <dt className="text-muted-foreground text-xs font-medium tracking-wide uppercase">
                {row.label}
              </dt>
              <dd className="text-sm text-pretty">{row.value}</dd>
            </div>
          ))}
        </dl>
      </CardContent>
    </Card>
  );
}
