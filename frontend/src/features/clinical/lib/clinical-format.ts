import type { ClinicalSummaryResponse } from '@/features/clinical/types/consultation';
import type { ConsultationStatus } from '@/features/clinical/types/enums';
import { EDITABLE_CONSULTATION_STATUSES } from '@/features/clinical/types/enums';
import type {
  CreatePrescriptionPayload,
  PrescriptionItemPayload,
} from '@/features/clinical/types/prescription';
import type { RecordVitalSignsPayload } from '@/features/clinical/types/vitals';
import type {
  ClinicalNoteFormValues,
  DiagnosisFormValues,
  DocumentationFormValues,
  FollowUpFormValues,
  PrescriptionFormValues,
  PrescriptionItemFormValues,
  StartConsultationFormValues,
  VitalsFormValues,
} from '@/features/clinical/validation/clinical-schemas';

export function isEditableConsultationStatus(status: ConsultationStatus): boolean {
  return EDITABLE_CONSULTATION_STATUSES.includes(status);
}

export function documentationFromSummary(
  summary: ClinicalSummaryResponse,
): DocumentationFormValues {
  return {
    chiefComplaint: summary.chiefComplaint ?? '',
    historyOfPresentIllness: summary.historyOfPresentIllness ?? '',
    physicalExamination: summary.physicalExamination ?? '',
    doctorNotes: summary.doctorNotes ?? '',
    summary: summary.summary ?? '',
    advice: summary.advice ?? '',
  };
}

export function toDocumentationPayload(values: DocumentationFormValues) {
  return {
    chiefComplaint: values.chiefComplaint?.trim() || null,
    historyOfPresentIllness: values.historyOfPresentIllness?.trim() || null,
    physicalExamination: values.physicalExamination?.trim() || null,
    doctorNotes: values.doctorNotes?.trim() || null,
    summary: values.summary?.trim() || null,
    advice: values.advice?.trim() || null,
  };
}

export function toCreateConsultationPayload(values: StartConsultationFormValues) {
  return {
    patientId: values.patientId,
    doctorId: values.doctorId,
    departmentId: values.departmentId,
    appointmentId: values.appointmentId?.trim() || null,
    chiefComplaint: values.chiefComplaint?.trim() || null,
    startImmediately: values.startImmediately,
  };
}

function toOptionalNumber(value: string | number | null | undefined): number | null {
  if (value === undefined || value === null || value === '') {
    return null;
  }
  const parsed = typeof value === 'number' ? value : Number(value);
  return Number.isFinite(parsed) ? parsed : null;
}

export function toVitalsPayload(values: VitalsFormValues): RecordVitalSignsPayload {
  return {
    temperatureCelsius: toOptionalNumber(values.temperatureCelsius),
    heartRateBpm: toOptionalNumber(values.heartRateBpm),
    systolicBp: toOptionalNumber(values.systolicBp),
    diastolicBp: toOptionalNumber(values.diastolicBp),
    respiratoryRate: toOptionalNumber(values.respiratoryRate),
    oxygenSaturationPercent: toOptionalNumber(values.oxygenSaturationPercent),
    heightCm: toOptionalNumber(values.heightCm),
    weightKg: toOptionalNumber(values.weightKg),
    painScale: toOptionalNumber(values.painScale),
    notes: values.notes?.trim() || null,
  };
}

export function toDiagnosisPayload(values: DiagnosisFormValues) {
  return {
    diagnosisName: values.diagnosisName.trim(),
    icdCode: values.icdCode?.trim() || null,
    diagnosisType: values.diagnosisType,
    diagnosisStatus: values.diagnosisStatus,
    severity: values.severity,
    clinicalNotes: values.clinicalNotes?.trim() || null,
  };
}

export function toClinicalNotePayload(values: ClinicalNoteFormValues) {
  return {
    noteType: values.noteType,
    title: values.title?.trim() || null,
    content: values.content.trim(),
  };
}

export function toFollowUpPayload(values: FollowUpFormValues) {
  const leadRaw = values.reminderLeadDays?.trim();
  const lead = leadRaw ? Number(leadRaw) : null;

  return {
    scheduledDate: values.scheduledDate,
    scheduledTime: values.scheduledTime?.trim() || null,
    status: values.status,
    priority: values.priority,
    reason: values.reason?.trim() || null,
    instructions: values.instructions?.trim() || null,
    clinicalRecommendations: values.clinicalRecommendations?.trim() || null,
    reminderEnabled: values.reminderEnabled,
    reminderLeadDays: lead !== null && Number.isFinite(lead) ? lead : null,
  };
}

function toPrescriptionItemPayload(item: PrescriptionItemFormValues): PrescriptionItemPayload {
  const refillsRaw = item.refills?.trim();
  return {
    medicineName: item.medicineName.trim(),
    dosage: item.dosage.trim(),
    frequency: item.frequency.trim(),
    route: item.route,
    duration: item.duration.trim(),
    instructions: item.instructions?.trim() || null,
    quantity: Number(item.quantity),
    refills: refillsRaw ? Number(refillsRaw) : null,
    beforeFood: item.beforeFood,
    afterFood: item.afterFood,
  };
}

export function toCreatePrescriptionPayload(
  consultationId: string,
  values: PrescriptionFormValues,
): CreatePrescriptionPayload {
  return {
    consultationId,
    notes: values.notes?.trim() || null,
    issueImmediately: values.issueImmediately,
    items: values.items.map(toPrescriptionItemPayload),
  };
}

export function formatBp(
  systolic: number | null | undefined,
  diastolic: number | null | undefined,
): string {
  if (systolic == null || diastolic == null) {
    return '—';
  }
  return `${systolic}/${diastolic}`;
}

export function formatInstant(value: string | null | undefined): string {
  if (!value) {
    return '—';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}
