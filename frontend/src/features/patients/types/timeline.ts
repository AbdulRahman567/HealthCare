import type { TimelineEventType, TimelineSeverityHint, TimelineSortDirection } from './enums';

export type TimelineEventResponse = {
  type: TimelineEventType;
  sourceId: string;
  occurredOn: string;
  recordedAt: string;
  title: string;
  summary: string;
  status: string | null;
  severityHint: TimelineSeverityHint;
  critical: boolean;
  recordedByUserId: string | null;
  detailPath: string | null;
  attributes: Record<string, string>;
};

export type TimelinePageResponse = {
  patientId: string;
  content: TimelineEventResponse[];
  size: number;
  hasNext: boolean;
  nextCursor: string | null;
};

export type TimelineQuery = {
  types?: TimelineEventType[];
  cursor?: string;
  size?: number;
  direction?: TimelineSortDirection;
};
