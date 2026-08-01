import { execFileSync } from 'child_process';
import { readFileSync } from 'fs';
import { expect, test } from '@playwright/test';

/**
 * Phase 9 full-stack regression walkthrough against a REAL backend + MySQL (no API mocks).
 *
 * Homepage -> pricing -> single signup -> submit (pending only) -> email verification ->
 * login -> dashboard. Each step is asserted via DOM, and the "no account before verification"
 * invariant is confirmed by a direct DB query between submit and verify.
 *
 * Prereqs (run first): backend on :8080 (HMS_MAIL_LOG_LINK=true, log at backend/target/backend9.log),
 * frontend on :3000, PLAYWRIGHT_SKIP_WEB_SERVER=1.
 */
const BACKEND_LOG = 'D:/AbdulRahman/Healthcare-HMS/backend/target/backend9.log';
const DBCHECK = 'D:/AbdulRahman/Healthcare-HMS/backend/target/db-helper/dbcheck.mjs';
const DB_CWD = 'D:/AbdulRahman/Healthcare-HMS/backend/target/db-helper';

const RUN = Date.now();
const ADMIN_EMAIL = `phase9.${RUN}@regress.local`;
const HOSPITAL_EMAIL = `phase9.${RUN}.hosp@regress.local`;
const PASSWORD = 'StrongPass1!ab';

function dbCounts() {
  const out = execFileSync('node', [DBCHECK, ADMIN_EMAIL, HOSPITAL_EMAIL], {
    cwd: DB_CWD,
    encoding: 'utf8',
  });
  return JSON.parse(out.trim());
}

function readVerifyToken() {
  const log = readFileSync(BACKEND_LOG, 'utf8');
  const match = log.match(/verify-registration\?token=([0-9a-f]{96})/);
  if (!match) {
    throw new Error('Verification token not found in backend log');
  }
  return match[1];
}

// Requires the full stack running (backend on :8080 + MySQL, frontend on :3000). Skips in the
// default Playwright run; enable with PHASE9_REAL=1 when the real stack is up.
test.describe('Phase 9 full user journey (real stack)', () => {
  test.skip(!process.env.PHASE9_REAL, 'set PHASE9_REAL=1 when the real stack is running');
  test('homepage -> pricing -> signup -> verify -> login -> dashboard', async ({ page }) => {
    // ---- 1. Homepage ----
    await page.goto('/', { waitUntil: 'domcontentloaded' });
    await expect(page.getByRole('banner').getByRole('link', { name: 'Pricing' })).toBeVisible();

    // ---- 2. Pricing ----
    await page.getByRole('banner').getByRole('link', { name: 'Pricing' }).click();
    await expect(page.getByRole('heading', { name: /Pricing/i })).toBeVisible();

    // Pick a plan -> single signup page with the plan applied (Standard CTA goes to ?plan=STANDARD).
    await page.goto('/register/hospital?plan=STANDARD', { waitUntil: 'domcontentloaded' });
    await expect(page.getByText(/Selected plan: Standard/i)).toBeVisible();

    // ---- 3. Single signup (Your Account + Hospital Details sections) ----
    await expect(page.getByRole('heading', { name: 'Your Account', exact: true })).toBeVisible();
    await expect(
      page.getByRole('heading', { name: 'Hospital Details', exact: true }),
    ).toBeVisible();

    const accountSection = page.getByRole('region', { name: 'Your Account' });
    const hospitalSection = page.getByRole('region', { name: 'Hospital Details' });
    await accountSection.getByLabel('First name').fill('Regression');
    await accountSection.getByLabel('Last name').fill('Walker');
    await accountSection.getByLabel('Email', { exact: true }).fill(ADMIN_EMAIL);
    await accountSection.getByLabel('Password').fill(PASSWORD);
    await hospitalSection.getByLabel('Hospital name').fill('Regression Walkthrough Hospital');
    await hospitalSection.getByLabel('Hospital email', { exact: true }).fill(HOSPITAL_EMAIL);

    await page.getByRole('button', { name: 'Create account' }).click();

    // Success screen explicitly says no account exists yet.
    await expect(page.getByText('Check your inbox', { exact: true })).toBeVisible({
      timeout: 10_000,
    });
    await expect(page.getByText(ADMIN_EMAIL)).toBeVisible();

    // ---- 4. DB PROOF: no tenant/user/account exists BEFORE verification; only a pending row ----
    const before = dbCounts();
    console.log('[DB before verification]', JSON.stringify(before));
    expect(before.tenants).toBe(0);
    expect(before.users).toBe(0);
    expect(before.pending).toBe(1);

    // ---- 5. Email verification (real link from backend log) ----
    const token = readVerifyToken();
    await page.goto(`/verify-registration?token=${token}`, { waitUntil: 'domcontentloaded' });
    await expect(page.getByText('Your hospital account is ready')).toBeVisible({ timeout: 15_000 });
    await expect(page.getByText(new RegExp(ADMIN_EMAIL.replace('.', '\\.')))).toBeVisible();

    // ---- 6. DB PROOF: real records created AFTER verification; pending consumed ----
    const after = dbCounts();
    console.log('[DB after verification]', JSON.stringify(after));
    expect(after.tenants).toBe(1);
    expect(after.users).toBe(1);
    expect(after.pending).toBe(0);

    // Activate the tenant (new tenants start PENDING and must be activated before login).
    const activateOut = execFileSync('node', [DBCHECK, ADMIN_EMAIL, HOSPITAL_EMAIL, 'activate'], {
      cwd: DB_CWD,
      encoding: 'utf8',
    });
    console.log('[DB tenant activate]', activateOut.trim());

    // ---- 7. Login as the newly-created admin -> dashboard ----
    await page.getByRole('link', { name: 'Continue to sign in' }).click();
    await expect(page.getByRole('heading', { name: 'Sign in' })).toBeVisible();
    await page.getByLabel('Email').fill(ADMIN_EMAIL);
    await page.getByLabel('Password').fill(PASSWORD);
    await page.getByRole('button', { name: 'Sign in' }).click();

    // Land on a protected dashboard route.
    await page.waitForURL(/\/app/, { timeout: 20_000 });
    await expect(page.getByRole('link', { name: 'Dashboard' })).toBeVisible({ timeout: 15_000 });
  });
});
