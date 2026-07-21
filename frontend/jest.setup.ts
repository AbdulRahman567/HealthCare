import '@testing-library/jest-dom';

process.env.NEXT_PUBLIC_API_URL ??= 'http://localhost:8080/api/v1';
process.env.NEXT_PUBLIC_APP_NAME ??= 'Healthcare HMS';
process.env.NEXT_PUBLIC_ENV ??= 'development';

// Base UI primitives (used by Button/Input/etc.) probe these browser APIs;
// jsdom does not implement them, so provide lightweight stand-ins for tests.
if (typeof window.matchMedia !== 'function') {
  window.matchMedia = jest.fn().mockImplementation((query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: jest.fn(),
    removeListener: jest.fn(),
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  }));
}

if (typeof window.ResizeObserver !== 'function') {
  class ResizeObserverStub {
    observe(): void {}
    unobserve(): void {}
    disconnect(): void {}
  }
  window.ResizeObserver = ResizeObserverStub as unknown as typeof ResizeObserver;
}

if (typeof window.scrollTo !== 'function') {
  window.scrollTo = jest.fn();
}
