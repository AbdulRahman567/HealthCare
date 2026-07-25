import { Badge } from '@/components/ui/badge';
import { formatEnumLabel } from '@/lib/page-query';
import { cn } from '@/lib/utils';

const TONE_CLASS: Record<'success' | 'warning' | 'danger' | 'neutral' | 'info', string> = {
  success: 'bg-emerald-500/10 text-emerald-700 dark:text-emerald-300',
  warning: 'bg-amber-500/10 text-amber-700 dark:text-amber-300',
  danger: 'bg-destructive/10 text-destructive',
  neutral: 'bg-muted text-muted-foreground',
  info: 'bg-sky-500/10 text-sky-700 dark:text-sky-300',
};

function resolveTone(status: string): keyof typeof TONE_CLASS {
  switch (status) {
    case 'ACTIVE':
    case 'ACCEPTED':
      return 'success';
    case 'PENDING':
    case 'ON_LEAVE':
      return 'warning';
    case 'SUSPENDED':
    case 'LOCKED':
    case 'TERMINATED':
    case 'REJECTED':
    case 'CANCELLED':
    case 'EXPIRED':
    case 'INACTIVE':
      return 'danger';
    default:
      return 'neutral';
  }
}

type StatusBadgeProps = {
  status: string;
  className?: string;
};

export function StatusBadge({ status, className }: StatusBadgeProps) {
  return (
    <Badge
      variant="secondary"
      className={cn('rounded-md border-0 font-medium', TONE_CLASS[resolveTone(status)], className)}
    >
      {formatEnumLabel(status)}
    </Badge>
  );
}
