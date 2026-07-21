type AuthSessionListener = () => void;

const listeners = new Set<AuthSessionListener>();

/**
 * Subscribe to forced session invalidation (e.g. refresh-token failure).
 * Returns an unsubscribe function.
 */
export function onAuthSessionInvalidated(listener: AuthSessionListener): () => void {
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
  };
}

export function emitAuthSessionInvalidated(): void {
  listeners.forEach((listener) => {
    try {
      listener();
    } catch {
      // Listener failures must not break the HTTP interceptor path.
    }
  });
}
