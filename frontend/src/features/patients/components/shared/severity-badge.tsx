import { Badge } from '@/components/ui/badge';
import { formatEnumLabel } from '@/lib/page-query';
import { cn } from '@/lib/utils';

const TONE_CLASS = {
  success: 'bg-emerald-500/10 text-emerald-700 dark:text-emerald-300',
  warning: 'bg-amber-500/10 text-amber-700 dark:text-amber-300',
  danger: 'bg-destructive/10 text-destructive',
  neutral: 'bg-muted text-muted-foreground',
  info: 'bg-sky-500/10 text-sky-700 dark:text-sky-300',
} as const;

function resolveSeverityTone(severity: string): keyof typeof TONE_CLASS {
  switch (severity) {
    case 'MILD':
    case 'NONE':
    case 'STANDARD':
      return 'info';
    case 'MODERATE':
    case 'HIGH':
      return 'warning';
    case 'SEVERE':
    case 'CRITICAL':
    case 'LIFE_THREATENING':
      return 'danger';
    default:
      return 'neutral';
  }
}

type SeverityBadgeProps = {
  severity: string;
  className?: string;
};

export function SeverityBadge({ severity, className }: SeverityBadgeProps) {
  return (
    <Badge
      variant="secondary"
      className={cn(
        'rounded-md border-0 font-medium',
        TONE_CLASS[resolveSeverityTone(severity)],
        className,
      )}
    >
      {formatEnumLabel(severity)}
    </Badge>
  );
}
