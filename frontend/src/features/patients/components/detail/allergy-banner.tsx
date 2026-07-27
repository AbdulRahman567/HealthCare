'use client';

import { AlertTriangleIcon, ShieldAlertIcon } from 'lucide-react';

import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { useAllergyBannerQuery } from '@/features/patients/hooks/use-allergies';
import { SeverityBadge } from '@/features/patients/components/shared/severity-badge';
import { formatEnumLabel } from '@/lib/page-query';
import { getErrorMessage } from '@/lib/api-error';
import { cn } from '@/lib/utils';

type AllergyBannerProps = {
  patientId: string;
};

export function AllergyBanner({ patientId }: AllergyBannerProps) {
  const bannerQuery = useAllergyBannerQuery(patientId);

  if (bannerQuery.isLoading) {
    return (
      <div
        className="rounded-xl border border-dashed px-4 py-3 text-sm text-muted-foreground"
        role="status"
        aria-live="polite"
      >
        Loading allergy alerts…
      </div>
    );
  }

  if (bannerQuery.isError) {
    return (
      <Alert variant="destructive">
        <ShieldAlertIcon />
        <AlertTitle>Allergy banner unavailable</AlertTitle>
        <AlertDescription>
          {getErrorMessage(
            bannerQuery.error,
            'Unable to load allergy alerts. Do not prescribe until reviewed.',
          )}
        </AlertDescription>
      </Alert>
    );
  }

  const banner = bannerQuery.data;
  if (!banner) {
    return null;
  }

  const hasBannerAllergies = banner.bannerAllergies.length > 0;
  const isCritical = banner.hasCriticalAlerts;

  return (
    <section
      aria-label="Allergy safety banner"
      className={cn(
        'rounded-xl border px-4 py-3',
        isCritical
          ? 'border-destructive/40 bg-destructive/5'
          : hasBannerAllergies
            ? 'border-amber-500/40 bg-amber-500/5'
            : 'border-border bg-muted/30',
      )}
    >
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div className="flex items-start gap-2">
          {isCritical ? (
            <AlertTriangleIcon className="text-destructive mt-0.5 size-5 shrink-0" aria-hidden />
          ) : (
            <ShieldAlertIcon className="text-muted-foreground mt-0.5 size-5 shrink-0" aria-hidden />
          )}
          <div className="space-y-1">
            <h2 className="text-sm font-semibold">
              {isCritical
                ? `Critical allergy alerts (${banner.criticalAlertCount})`
                : hasBannerAllergies
                  ? 'Active allergies on chart'
                  : 'No banner allergies'}
            </h2>
            <p className="text-muted-foreground text-xs">
              {banner.noKnownDrugAllergies
                ? 'No known drug allergies (NKDA) on active chart records.'
                : banner.hasActiveDrugAllergies
                  ? 'Active drug allergies present — verify before prescribing.'
                  : 'Review allergy list before clinical decisions.'}
            </p>
          </div>
        </div>
        <div className="flex flex-wrap gap-1.5">
          {banner.hasCriticalAlerts ? <Badge variant="destructive">Critical</Badge> : null}
          {banner.noKnownDrugAllergies ? <Badge variant="secondary">NKDA</Badge> : null}
          {banner.hasActiveDrugAllergies ? <Badge variant="secondary">Drug allergies</Badge> : null}
        </div>
      </div>

      {hasBannerAllergies ? (
        <ul className="mt-3 grid gap-2 sm:grid-cols-2">
          {banner.bannerAllergies.map((allergy) => (
            <li key={allergy.id} className="rounded-lg border bg-background/80 px-3 py-2 text-sm">
              <div className="flex flex-wrap items-center gap-2">
                <span className="font-medium">{allergy.allergenName}</span>
                <SeverityBadge severity={allergy.severity} />
                <Badge variant="outline">{formatEnumLabel(allergy.allergyType)}</Badge>
              </div>
              <p className="text-muted-foreground mt-1 text-xs">
                Reaction: {formatEnumLabel(allergy.reaction)}
                {allergy.criticalAlert ? ' · Critical alert' : ''}
              </p>
            </li>
          ))}
        </ul>
      ) : null}
    </section>
  );
}
