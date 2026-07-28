import type { Metadata } from 'next';

import { PrescriptionPrintPage } from '@/features/clinical/components/prescriptions/prescription-print-page';

export const metadata: Metadata = {
  title: 'Print Prescription | Healthcare HMS',
  description: 'Printable digital prescription',
};

type PrescriptionPrintRoutePageProps = {
  params: Promise<{ id: string }>;
};

export default async function PrescriptionPrintRoutePage({
  params,
}: PrescriptionPrintRoutePageProps) {
  const { id } = await params;
  return <PrescriptionPrintPage prescriptionId={id} />;
}
