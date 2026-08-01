import { chromium } from 'playwright';
import { writeFileSync } from 'fs';

// Phase 5 — Full user journey walkthrough (UI) with screenshots.
// homepage -> pricing -> signup (admin account) -> hospital setup -> confirm -> success
// Saves screenshots to audit-screenshots/phase5-*.png and writes the chosen
// credentials to audit-screenshots/phase5-state.json for the verification+login step.

const BASE = 'http://localhost:3000';
const SHOT_DIR = 'audit-screenshots';
const ts = Date.now();
const adminEmail = `phase5admin${ts}@hms.test`;
const hospitalEmail = `phase5hospital${ts}@hms.test`;
const hospitalName = `Phase5 Regression ${ts}`;
const password = 'Phase5!Admin2026';

const browser = await chromium.launch({ args: ['--no-sandbox'] });
const ctx = await browser.newContext({
  viewport: { width: 1440, height: 900 },
  deviceScaleFactor: 1,
});
const page = await ctx.newPage();
await page.route('**/*.woff2', (r) => r.abort());

const shot = (name) => page.screenshot({ path: `${SHOT_DIR}/phase5-${name}.png`, fullPage: false });

const consoleErrors = [];
page.on('console', (m) => m.type() === 'error' && consoleErrors.push(m.text()));
page.on('pageerror', (e) => consoleErrors.push('PAGEERROR: ' + e.message));

try {
  // 1. Homepage
  await page.goto(BASE, { waitUntil: 'networkidle' });
  await page.waitForSelector('main h1', { timeout: 15000 });
  await shot('01-homepage');

  // 2. Pricing page
  await page.goto(`${BASE}/pricing`, { waitUntil: 'networkidle' });
  await page.waitForSelector('h1', { timeout: 15000 });
  await shot('02-pricing');

  // 3. Signup — admin account step (choose STANDARD plan to exercise trial banner)
  await page.goto(`${BASE}/register/hospital?plan=STANDARD`, { waitUntil: 'networkidle' });
  await page.waitForSelector('#adminFirstName', { timeout: 15000 });
  await page.fill('#adminFirstName', 'Phase');
  await page.fill('#adminLastName', 'FiveAdmin');
  await page.fill('#adminEmail', adminEmail);
  await page.fill('#adminPassword', password);
  await page.fill('#adminPhone', '+1-555-0101');
  await shot('03-signup-admin-account');
  await page.getByRole('button', { name: 'Continue' }).click();

  // 4. Hospital setup step
  await page.waitForSelector('#hospitalName', { timeout: 15000 });
  await page.fill('#hospitalName', hospitalName);
  await page.fill('#hospitalEmail', hospitalEmail);
  await page.fill('#hospitalPhone', '+1-555-0102');
  await page.fill('#hospitalAddress', '120 Medical Center Drive');
  await shot('04-hospital-setup');
  await page.getByRole('button', { name: 'Continue' }).click();

  // 5. Review & confirm step
  await page.waitForSelector('text=Review and confirm', { timeout: 15000 });
  await shot('05-review-confirm');
  await page.getByRole('button', { name: 'Create account' }).click();

  // 6. Success screen
  await page.waitForSelector('text=Hospital registered', { timeout: 20000 });
  await page.waitForTimeout(500);
  await shot('06-registration-success');

  // Persist state for the verification + login step.
  writeFileSync(
    `${SHOT_DIR}/phase5-state.json`,
    JSON.stringify(
      {
        adminEmail,
        hospitalEmail,
        hospitalName,
        password,
        consoleErrors,
      },
      null,
      2,
    ),
  );

  console.log('WALKTHROUGH_OK');
  console.log('adminEmail=' + adminEmail);
  console.log('hospitalName=' + hospitalName);
  console.log('consoleErrors=' + JSON.stringify(consoleErrors));
} catch (err) {
  console.error('WALKTHROUGH_FAILED: ' + (err?.message || err));
  try {
    await shot('99-failure');
  } catch {}
  process.exit(1);
} finally {
  await browser.close();
}
