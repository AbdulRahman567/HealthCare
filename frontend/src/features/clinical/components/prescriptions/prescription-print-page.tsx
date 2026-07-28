'use client';

import { Loader2Icon, PrinterIcon } from 'lucide-react';
import Link from 'next/link';
import { useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';

import { Button } from '@/components/ui/button';
import { prescriptionsApi } from '@/features/clinical/api/prescriptions-api';
import { prescriptionKeys } from '@/features/clinical/hooks/use-prescriptions';
import { EmptyState } from '@/features/hospital-admin/components/shared/empty-state';
import { StatusBadge } from '@/features/hospital-admin/components/shared/status-badge';
import { getErrorMessage } from '@/lib/api-error';
import { formatEnumLabel } from '@/lib/page-query';

type PrescriptionPrintPageProps = {
  prescriptionId: string;
};

export function PrescriptionPrintPage({ prescriptionId }: PrescriptionPrintPageProps) {
  const query = useQuery({
    queryKey: prescriptionKeys.detail(prescriptionId),
    queryFn: () => prescriptionsApi.getById(prescriptionId),
    enabled: Boolean(prescriptionId),
  });

  useEffect(() => {
    if (!query.data) {
      return;
    }
    const timer = window.setTimeout(() => window.print(), 400);
    return () => window.clearTimeout(timer);
  }, [query.data]);

  if (query.isError) {
    return (
      <div className="mx-auto max-w-3xl p-6">
        <EmptyState
          title="Prescription not found"
          description={getErrorMessage(query.error)}
          action={
            <Button nativeButton={false} variant="outline" render={<Link href="/app/clinical" />}>
              Back
            </Button>
          }
        />
      </div>
    );
  }

  if (query.isLoading || !query.data) {
    return (
      <div className="text-muted-foreground flex items-center justify-center gap-2 py-24 text-sm">
        <Loader2Icon className="size-4 animate-spin" />
        Loading prescription…
      </div>
    );
  }

  const rx = query.data;

  return (
    <div className="mx-auto max-w-3xl space-y-6 bg-white p-6 text-black print:max-w-none print:p-0">
      <div className="flex flex-wrap items-center justify-between gap-2 print:hidden">
        <Button
          nativeButton={false}
          variant="outline"
          render={
            <Link
              href={
                rx.consultationId
                  ? `/app/clinical/${rx.consultationId}`
                  : `/app/patients/${rx.patientId}`
              }
            />
          }
        >
          Back
        </Button>
        <Button type="button" onClick={() => window.print()}>
          <PrinterIcon data-icon="inline-start" />
          Print
        </Button>
      </div>

      <header className="space-y-1 border-b border-neutral-300 pb-4">
        <p className="text-xs tracking-[0.2em] text-neutral-500 uppercase">Healthcare HMS</p>
        <h1 className="text-2xl font-semibold tracking-tight">Prescription</h1>
        <div className="flex flex-wrap items-center gap-2 text-sm">
          <span className="font-mono font-semibold">{rx.prescriptionNumber}</span>
          <StatusBadge status={rx.status} />
          <span className="text-neutral-600">{rx.prescriptionDate}</span>
        </div>
      </header>

      <section className="grid gap-3 text-sm sm:grid-cols-2">
        <PrintField label="Patient" value={`${rx.patientName} · ${rx.patientMrn}`} />
        <PrintField label="Doctor" value={rx.doctorName} />
        <PrintField label="Department" value={rx.departmentName} />
        <PrintField label="Consultation" value={rx.consultationNumber} />
        {rx.issuedAt ? (
          <PrintField label="Issued at" value={new Date(rx.issuedAt).toLocaleString()} />
        ) : null}
      </section>

      <section>
        <h2 className="mb-2 text-sm font-semibold tracking-wide uppercase">Medicines</h2>
        <table className="w-full border-collapse text-left text-sm">
          <thead>
            <tr className="border-b border-neutral-300">
              <th className="py-2 pr-2 font-medium">#</th>
              <th className="py-2 pr-2 font-medium">Medicine</th>
              <th className="py-2 pr-2 font-medium">Dosage</th>
              <th className="py-2 pr-2 font-medium">Frequency</th>
              <th className="py-2 pr-2 font-medium">Route</th>
              <th className="py-2 pr-2 font-medium">Duration</th>
              <th className="py-2 pr-2 font-medium">Qty</th>
              <th className="py-2 font-medium">Instructions</th>
            </tr>
          </thead>
          <tbody>
            {rx.items.map((item, index) => (
              <tr key={item.id} className="border-b border-neutral-200 align-top">
                <td className="py-2 pr-2 tabular-nums">{index + 1}</td>
                <td className="py-2 pr-2 font-medium">{item.medicineName}</td>
                <td className="py-2 pr-2">{item.dosage}</td>
                <td className="py-2 pr-2">{item.frequency}</td>
                <td className="py-2 pr-2">{formatEnumLabel(item.route)}</td>
                <td className="py-2 pr-2">{item.duration}</td>
                <td className="py-2 pr-2 tabular-nums">{item.quantity}</td>
                <td className="py-2">
                  {[
                    item.instructions,
                    item.beforeFood ? 'Before food' : null,
                    item.afterFood ? 'After food' : null,
                    item.refills != null ? `Refills: ${item.refills}` : null,
                  ]
                    .filter(Boolean)
                    .join(' · ') || '—'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      {rx.notes ? (
        <section className="text-sm">
          <h2 className="mb-1 text-sm font-semibold tracking-wide uppercase">Notes</h2>
          <p className="whitespace-pre-wrap">{rx.notes}</p>
        </section>
      ) : null}

      <footer className="mt-10 grid gap-8 border-t border-neutral-300 pt-6 text-sm sm:grid-cols-2">
        <div>
          <p className="text-neutral-500">Prescribing doctor</p>
          <p className="mt-8 border-t border-neutral-400 pt-1 font-medium">{rx.doctorName}</p>
        </div>
        <div>
          <p className="text-neutral-500">Date</p>
          <p className="mt-8 border-t border-neutral-400 pt-1 font-medium">{rx.prescriptionDate}</p>
        </div>
      </footer>
    </div>
  );
}

function PrintField({ label, value }: { label: string; value: string | null | undefined }) {
  return (
    <div>
      <p className="text-xs tracking-wide text-neutral-500 uppercase">{label}</p>
      <p className="mt-0.5 font-medium text-pretty">{value?.trim() || '—'}</p>
    </div>
  );
}
