'use client';

import { CloudIcon, CloudOffIcon, KeyboardIcon } from 'lucide-react';

import type { AutoSaveStatus } from '@/features/clinical/hooks/use-auto-save-draft';
import { formatInstant } from '@/features/clinical/lib/clinical-format';
import { cn } from '@/lib/utils';

type DraftSaveIndicatorProps = {
  status: AutoSaveStatus;
  lastSavedAt: string | null;
  className?: string;
};

export function DraftSaveIndicator({ status, lastSavedAt, className }: DraftSaveIndicatorProps) {
  const label =
    status === 'saving'
      ? 'Saving draft…'
      : status === 'restored'
        ? 'Draft restored'
        : status === 'saved' && lastSavedAt
          ? `Draft saved ${formatInstant(lastSavedAt)}`
          : 'Draft auto-save on';

  return (
    <div
      className={cn(
        'text-muted-foreground inline-flex items-center gap-1.5 text-xs',
        className,
      )}
      role="status"
      aria-live="polite"
    >
      {status === 'saving' ? (
        <CloudIcon className="size-3.5 animate-pulse" aria-hidden />
      ) : (
        <CloudOffIcon className="size-3.5 opacity-60" aria-hidden />
      )}
      <span>{label}</span>
    </div>
  );
}

export function KeyboardShortcutsHint({ className }: { className?: string }) {
  return (
    <p
      className={cn(
        'text-muted-foreground inline-flex flex-wrap items-center gap-x-3 gap-y-1 text-xs',
        className,
      )}
    >
      <span className="inline-flex items-center gap-1">
        <KeyboardIcon className="size-3.5" aria-hidden />
        Shortcuts
      </span>
      <kbd className="rounded border bg-muted px-1.5 py-0.5 font-mono text-[10px]">Ctrl+S</kbd>
      <span>save chart</span>
      <kbd className="rounded border bg-muted px-1.5 py-0.5 font-mono text-[10px]">1–7</kbd>
      <span>switch tabs</span>
      <kbd className="rounded border bg-muted px-1.5 py-0.5 font-mono text-[10px]">Alt+N</kbd>
      <span>focus next field</span>
    </p>
  );
}
