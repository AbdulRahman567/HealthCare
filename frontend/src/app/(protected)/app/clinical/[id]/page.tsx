import type { Metadata } from 'next';

import { ConsultationWorkspacePage } from '@/features/clinical';

export const metadata: Metadata = {
  title: 'Consultation | Healthcare HMS',
  description: 'Clinical consultation workspace',
};

type ConsultationRouteProps = {
  params: Promise<{ id: string }>;
};

export default async function ConsultationWorkspaceRoutePage({ params }: ConsultationRouteProps) {
  const { id } = await params;
  return <ConsultationWorkspacePage consultationId={id} />;
}
