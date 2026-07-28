'use client';

import { useEffect, useRef, useState } from 'react';
import type { FieldValues, UseFormWatch } from 'react-hook-form';

import { clearDraft, readDraft, writeDraft } from '@/features/clinical/lib/draft-storage';

const DEFAULT_DEBOUNCE_MS = 800;

type UseAutoSaveDraftOptions<T extends FieldValues> = {
  consultationId: string;
  tab: string;
  watch: UseFormWatch<T>;
  enabled?: boolean;
  debounceMs?: number;
  onRestore?: (values: T) => void;
};

export type AutoSaveStatus = 'idle' | 'saving' | 'saved' | 'restored';

/**
 * Debounced local draft persistence for clinical forms.
 * Restores once on mount when a draft exists; call clearSavedDraft after successful server save.
 */
export function useAutoSaveDraft<T extends FieldValues>({
  consultationId,
  tab,
  watch,
  enabled = true,
  debounceMs = DEFAULT_DEBOUNCE_MS,
  onRestore,
}: UseAutoSaveDraftOptions<T>) {
  const [status, setStatus] = useState<AutoSaveStatus>('idle');
  const [lastSavedAt, setLastSavedAt] = useState<string | null>(null);
  const readyRef = useRef(false);
  const skipNextSaveRef = useRef(false);
  const values = watch();
  const serialized = JSON.stringify(values);

  useEffect(() => {
    if (!enabled || !consultationId || readyRef.current) {
      return;
    }
    const draft = readDraft<T>(consultationId, tab);
    if (draft?.values) {
      onRestore?.(draft.values);
      skipNextSaveRef.current = true;
      setStatus('restored');
      setLastSavedAt(draft.meta.updatedAt);
    }
    readyRef.current = true;
  }, [consultationId, enabled, onRestore, tab]);

  useEffect(() => {
    if (!enabled || !consultationId || !readyRef.current) {
      return;
    }
    if (skipNextSaveRef.current) {
      skipNextSaveRef.current = false;
      return;
    }
    setStatus('saving');
    const handle = window.setTimeout(() => {
      writeDraft(consultationId, tab, JSON.parse(serialized) as T);
      const now = new Date().toISOString();
      setLastSavedAt(now);
      setStatus('saved');
    }, debounceMs);
    return () => window.clearTimeout(handle);
  }, [consultationId, debounceMs, enabled, serialized, tab]);

  const clearSavedDraft = () => {
    clearDraft(consultationId, tab);
    setStatus('idle');
  };

  return { status, lastSavedAt, clearSavedDraft };
}
