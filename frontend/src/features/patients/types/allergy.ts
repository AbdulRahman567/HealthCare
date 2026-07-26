import type { AllergyReaction, AllergySeverity, AllergyStatus, AllergyType } from './enums';

export type AllergyResponse = {
  id: string;
  patientId: string;
  allergenName: string;
  allergenCode: string | null;
  allergyType: AllergyType;
  severity: AllergySeverity;
  reaction: AllergyReaction;
  status: AllergyStatus;
  onsetDate: string | null;
  clinicalNotes: string | null;
  verified: boolean;
  patientReported: boolean;
  criticalAlert: boolean;
  showOnBanner: boolean;
  lifeThreatening: boolean;
  recordedByUserId: string | null;
  createdAt: string;
  updatedAt: string;
  version: number;
};

export type AllergyBannerResponse = {
  patientId: string;
  hasCriticalAlerts: boolean;
  criticalAlertCount: number;
  hasActiveDrugAllergies: boolean;
  noKnownDrugAllergies: boolean;
  bannerAllergies: AllergyResponse[];
};

export type AllergyCriticalAlertResponse = {
  patientId: string;
  criticalAlertCount: number;
  criticalAllergies: AllergyResponse[];
};

export type UpsertAllergyPayload = {
  allergenName: string;
  allergenCode?: string | null;
  allergyType: AllergyType;
  severity: AllergySeverity;
  reaction: AllergyReaction;
  status?: AllergyStatus;
  onsetDate?: string | null;
  clinicalNotes?: string | null;
  verified?: boolean;
  patientReported?: boolean;
  criticalAlert?: boolean;
  showOnBanner?: boolean;
};
