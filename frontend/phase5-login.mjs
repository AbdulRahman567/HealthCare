import { chromium } from 'playwright';

// Phase 5 — Login + dashboard (post-verification). Screenshots step 7 & 8.
const BASE = 'http://localhost:3000';
const SHOT_DIR = 'audit-screenshots';
const email = 'phase5admin1785545804888@hms.test';
const password = 'Phase5!Admin2026';

const browser = await chromium.launch({ args: ['--no-sandbox'] });
const ctx = await browser.newContext({ viewport: { width: 1440, height: 900 } });
const page = await ctx.newPage();
await page.route('**/*.woff2', (r) => r.abort());

const consoleErrors = [];
page.on('console', (m) => m.type() === 'error' && consoleErrors.push(m.text()));
page.on('pageerror', (e) => consoleErrors.push('PAGEERROR: ' + e.message));

try {
  await page.goto(`${BASE}/login`, { waitUntil: 'networkidle' });
  await page.waitForSelector('#email', { timeout: 15000 });
  await page.fill('#email', email);
  await page.fill('#password', password);
  await page.screenshot({ path: `${SHOT_DIR}/phase5-07-login.png` });

  await page.getByRole('button', { name: /Sign in/i }).click();

  // Wait for the protected dashboard to load.
  await page.waitForURL('**/app**', { timeout: 20000 });
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(1500);
  await page.screenshot({ path: `${SHOT_DIR}/phase5-08-dashboard.png`, fullPage: true });

  const url = page.url();
  const heading = await page
    .locator('h1')
    .first()
    .textContent()
    .catch(() => 'N/A');
  console.log('LOGIN_OK');
  console.log('url=' + url);
  console.log('h1=' + heading);
  console.log('consoleErrors=' + JSON.stringify(consoleErrors));
} catch (err) {
  console.error('LOGIN_FAILED: ' + (err?.message || err));
  try {
    await page.screenshot({ path: `${SHOT_DIR}/phase5-08-failure.png` });
  } catch {}
  process.exit(1);
} finally {
  await browser.close();
}
