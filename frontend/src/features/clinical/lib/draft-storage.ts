const DRAFT_PREFIX = 'hms.clinical.draft.';

export type DraftMeta = {
  updatedAt: string;
  consultationId: string;
  tab: string;
};

export type StoredDraft<T> = {
  meta: DraftMeta;
  values: T;
};

function storageKey(consultationId: string, tab: string): string {
  return `${DRAFT_PREFIX}${consultationId}.${tab}`;
}

export function readDraft<T>(consultationId: string, tab: string): StoredDraft<T> | null {
  if (typeof window === 'undefined') {
    return null;
  }
  try {
    const raw = window.localStorage.getItem(storageKey(consultationId, tab));
    if (!raw) {
      return null;
    }
    return JSON.parse(raw) as StoredDraft<T>;
  } catch {
    return null;
  }
}

export function writeDraft<T>(consultationId: string, tab: string, values: T): void {
  if (typeof window === 'undefined') {
    return;
  }
  const payload: StoredDraft<T> = {
    meta: {
      consultationId,
      tab,
      updatedAt: new Date().toISOString(),
    },
    values,
  };
  window.localStorage.setItem(storageKey(consultationId, tab), JSON.stringify(payload));
}

export function clearDraft(consultationId: string, tab: string): void {
  if (typeof window === 'undefined') {
    return;
  }
  window.localStorage.removeItem(storageKey(consultationId, tab));
}

/** Clears all clinical chart drafts (call on logout to reduce shared-workstation PHI residual). */
export function clearAllClinicalDrafts(): void {
  if (typeof window === 'undefined') {
    return;
  }
  const keys: string[] = [];
  for (let i = 0; i < window.localStorage.length; i += 1) {
    const key = window.localStorage.key(i);
    if (key?.startsWith(DRAFT_PREFIX)) {
      keys.push(key);
    }
  }
  for (const key of keys) {
    window.localStorage.removeItem(key);
  }
}
