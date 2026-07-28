import type { Metadata } from 'next';

import { StartConsultationPage } from '@/features/clinical';

export const metadata: Metadata = {
  title: 'Start Consultation | Healthcare HMS',
  description: 'Start a new clinical consultation',
};

type StartConsultationRoutePageProps = {
  searchParams: Promise<{
    appointmentId?: string;
    patientId?: string;
    doctorId?: string;
    departmentId?: string;
  }>;
};

export default async function StartConsultationRoutePage({
  searchParams,
}: StartConsultationRoutePageProps) {
  const params = await searchParams;
  return (
    <StartConsultationPage
      initialAppointmentId={params.appointmentId}
      initialPatientId={params.patientId}
      initialDoctorId={params.doctorId}
      initialDepartmentId={params.departmentId}
    />
  );
}
