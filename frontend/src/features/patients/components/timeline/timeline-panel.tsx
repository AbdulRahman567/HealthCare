'use client';

import { Loader2Icon } from 'lucide-react';
import { useMemo } from 'react';

import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { EmptyState } from '@/features/hospital-admin/components/shared/empty-state';
import { SeverityBadge } from '@/features/patients/components/shared/severity-badge';
import { useTimelineInfiniteQuery } from '@/features/patients/hooks/use-timeline';
import { formatDate } from '@/features/patients/lib/patient-format';
import { getErrorMessage } from '@/lib/api-error';
import { formatEnumLabel } from '@/lib/page-query';
import { cn } from '@/lib/utils';

type TimelinePanelProps = {
  patientId: string;
};

export function TimelinePanel({ patientId }: TimelinePanelProps) {
  const timelineQuery = useTimelineInfiniteQuery(patientId, { size: 20, direction: 'DESC' });

  const events = useMemo(
    () => timelineQuery.data?.pages.flatMap((page) => page.content) ?? [],
    [timelineQuery.data],
  );

  if (timelineQuery.isError) {
    return (
      <EmptyState
        title="Unable to load timeline"
        description={getErrorMessage(timelineQuery.error)}
      />
    );
  }

  if (timelineQuery.isLoading) {
    return <div className="text-muted-foreground py-10 text-center text-sm">Loading timeline…</div>;
  }

  if (events.length === 0) {
    return (
      <EmptyState
        title="No timeline events yet"
        description="Registration, history, allergies, vaccinations, follow-ups, and clinical events appear here chronologically."
      />
    );
  }

  return (
    <div className="space-y-4">
      <ol className="relative space-y-4 border-l border-border pl-6">
        {events.map((event) => (
          <li key={`${event.type}-${event.sourceId}-${event.occurredOn}`} className="relative">
            <span
              className={cn(
                'absolute top-1.5 -left-[1.625rem] size-3 rounded-full border-2 border-background',
                event.critical || event.severityHint === 'CRITICAL'
                  ? 'bg-destructive'
                  : event.severityHint === 'HIGH'
                    ? 'bg-amber-500'
                    : 'bg-primary',
              )}
              aria-hidden
            />
            <article className="rounded-xl border bg-card p-4">
              <div className="flex flex-wrap items-start justify-between gap-2">
                <div className="space-y-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <h3 className="font-medium">{event.title}</h3>
                    <Badge variant="outline">{formatEnumLabel(event.type)}</Badge>
                    {event.critical ? <Badge variant="destructive">Critical</Badge> : null}
                    {event.severityHint !== 'NONE' && event.severityHint !== 'STANDARD' ? (
                      <SeverityBadge severity={event.severityHint} />
                    ) : null}
                  </div>
                  <p className="text-muted-foreground text-sm text-pretty">{event.summary}</p>
                </div>
                <time
                  className="text-muted-foreground shrink-0 text-xs"
                  dateTime={event.occurredOn}
                >
                  {formatDate(event.occurredOn)}
                </time>
              </div>
              {event.status ? (
                <p className="text-muted-foreground mt-2 text-xs">
                  Status: {formatEnumLabel(event.status)}
                </p>
              ) : null}
            </article>
          </li>
        ))}
      </ol>

      {timelineQuery.hasNextPage ? (
        <div className="flex justify-center">
          <Button
            type="button"
            variant="outline"
            disabled={timelineQuery.isFetchingNextPage}
            onClick={() => timelineQuery.fetchNextPage()}
          >
            {timelineQuery.isFetchingNextPage ? (
              <Loader2Icon className="animate-spin" data-icon="inline-start" />
            ) : null}
            Load more
          </Button>
        </div>
      ) : null}
    </div>
  );
}
