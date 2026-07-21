import { defineConfig, devices } from '@playwright/test';

/**
 * All auth e2e specs mock the backend via `page.route`, so this suite is
 * self-contained and does not require the Java backend to be running.
 * `webServer` starts the Next.js dev server automatically for CI/local runs;
 * set `PLAYWRIGHT_SKIP_WEB_SERVER=1` to attach to an already-running server.
 */
export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  workers: 1,
  timeout: 60_000,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  reporter: [['list'], ['html', { open: 'never', outputFolder: 'playwright-report' }]],
  use: {
    baseURL: process.env.PLAYWRIGHT_BASE_URL ?? 'http://localhost:3000',
    trace: 'on-first-retry',
    navigationTimeout: 45_000,
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
  webServer: process.env.PLAYWRIGHT_SKIP_WEB_SERVER
    ? undefined
    : {
        // Prefer production server for stable e2e (Turbopack/dev can reset mid-suite).
        command: 'npx next build && npx next start --port 3000',
        url: 'http://localhost:3000',
        reuseExistingServer: !process.env.CI,
        timeout: 300_000,
      },
});
