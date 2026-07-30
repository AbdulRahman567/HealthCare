'use client';

import { Loader2Icon, PrinterIcon } from 'lucide-react';
import Link from 'next/link';
import { useState } from 'react';

import { Button } from '@/components/ui/button';
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
import { usePatientPrescriptionsQuery } from '@/features/clinical/hooks/use-prescriptions';
import { EmptyState } from '@/features/hospital-admin/components/shared/empty-state';
import { StatusBadge } from '@/features/hospital-admin/components/shared/status-badge';
import { getErrorMessage } from '@/lib/api-error';

type PatientPrescriptionsPanelProps = {
  patientId: string;
};

export function PatientPrescriptionsPanel({ patientId }: PatientPrescriptionsPanelProps) {
  const [page, setPage] = useState(0);
  const query = usePatientPrescriptionsQuery(patientId, page, 10);

  if (query.isError) {
    return (
      <EmptyState title="Unable to load prescriptions" description={getErrorMessage(query.error)} />
    );
  }

  if (query.isLoading || !query.data) {
    return (
      <div className="text-muted-foreground flex items-center justify-center gap-2 py-10 text-sm">
        <Loader2Icon className="size-4 animate-spin" />
        Loading prescription history…
      </div>
    );
  }

  const pageData = query.data;
  const rows = pageData.content ?? [];

  if (rows.length === 0) {
    return (
      <EmptyState
        title="No prescriptions"
        description="Prescriptions issued during consultations will appear here."
      />
    );
  }

  return (
    <div className="space-y-4">
      <div className="overflow-x-auto rounded-xl border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Number</TableHead>
              <TableHead>Date</TableHead>
              <TableHead>Doctor</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Medicines</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {rows.map((rx) => (
              <TableRow key={rx.id}>
                <TableCell className="font-mono text-sm">{rx.prescriptionNumber}</TableCell>
                <TableCell>{rx.prescriptionDate}</TableCell>
                <TableCell>{rx.doctorName}</TableCell>
                <TableCell>
                  <StatusBadge status={rx.status} />
                </TableCell>
                <TableCell className="max-w-xs truncate text-sm">
                  {rx.items.map((item) => item.medicineName).join(', ') || '—'}
                </TableCell>
                <TableCell className="text-right">
                  <div className="flex flex-wrap justify-end gap-2">
                    {rx.consultationId ? (
                      <Button
                        nativeButton={false}
                        size="sm"
                        variant="ghost"
                        render={<Link href={`/app/clinical/${rx.consultationId}`} />}
                      >
                        Encounter
                      </Button>
                    ) : null}
                    <Can permissions={[Permissions.PRESCRIPTION_READ]}>
                      <Button
                        nativeButton={false}
                        size="sm"
                        variant="outline"
                        render={<Link href={`/app/prescriptions/${rx.id}/print`} target="_blank" />}
                      >
                        <PrinterIcon data-icon="inline-start" />
                        Print
                      </Button>
                    </Can>
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      <div className="flex items-center justify-between gap-2">
        <p className="text-muted-foreground text-xs">
          Page {pageData.page + 1} of {Math.max(pageData.totalPages, 1)} · {pageData.totalElements}{' '}
          total
        </p>
        <div className="flex gap-2">
          <Button
            type="button"
            size="sm"
            variant="outline"
            disabled={pageData.first}
            onClick={() => setPage((current) => Math.max(0, current - 1))}
          >
            Previous
          </Button>
          <Button
            type="button"
            size="sm"
            variant="outline"
            disabled={pageData.last}
            onClick={() => setPage((current) => current + 1)}
          >
            Next
          </Button>
        </div>
      </div>
    </div>
  );
}
