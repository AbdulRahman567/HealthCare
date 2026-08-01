import { expect, test } from '@playwright/test';

import { mockJson } from '../helpers/mock-api';

test.describe('Single-page hospital registration (Phase 7)', () => {
  test('renders both account and hospital sections on one scrollable page', async ({ page }) => {
    await page.goto('/register/hospital?plan=PREMIUM', { waitUntil: 'domcontentloaded' });

    // Phase 2 separation preserved on a single page: two clearly-labelled sections.
    await expect(page.getByRole('heading', { name: 'Your Account', exact: true })).toBeVisible();
    await expect(
      page.getByRole('heading', { name: 'Hospital Details', exact: true }),
    ).toBeVisible();

    // The selected plan from /pricing is applied.
    await expect(page.getByText('Selected plan: Premium')).toBeVisible();

    // Single submit button (no wizard steps / progress bar).
    await expect(page.getByRole('button', { name: 'Create account' })).toBeVisible();
    await expect(page.getByText(/wizard/i)).toHaveCount(0);
  });

  test('shows validation errors on an empty submit', async ({ page }) => {
    await page.goto('/register/hospital', { waitUntil: 'domcontentloaded' });
    await page.getByRole('button', { name: 'Create account' }).click();

    await expect(page.getByText('First name is required', { exact: true })).toBeVisible();
    await expect(page.getByText('Last name is required', { exact: true })).toBeVisible();
    await expect(page.getByText('Email is required', { exact: true })).toBeVisible();
    await expect(
      page.getByText('Password must be at least 12 characters', { exact: true }),
    ).toBeVisible();
    await expect(
      page.getByText('Hospital name must be at least 2 characters', { exact: true }),
    ).toBeVisible();
    await expect(page.getByText('Hospital email is required', { exact: true })).toBeVisible();
  });

  test('submitting creates a pending registration and shows a "check your inbox" success screen', async ({
    page,
  }) => {
    await mockJson(page, '**/register', {
      success: true,
      message: 'Registration received. Verify your email to create your hospital account.',
      timestamp: new Date().toISOString(),
      data: { email: 'admin@phase7.test', expiresInMinutes: 1440 },
    });

    await page.goto('/register/hospital?plan=STANDARD', { waitUntil: 'domcontentloaded' });

    const accountSection = page.getByRole('region', { name: 'Your Account' });
    const hospitalSection = page.getByRole('region', { name: 'Hospital Details' });

    // Your Account
    await accountSection.getByLabel('First name').fill('Phase');
    await accountSection.getByLabel('Last name').fill('Seven');
    await accountSection.getByLabel('Email', { exact: true }).fill('admin@phase7.test');
    await accountSection.getByLabel('Password').fill('StrongPass1!ab');
    await accountSection.getByLabel('Phone (optional)').fill('+1234567890');

    // Hospital Details
    await hospitalSection.getByLabel('Hospital name').fill('Phase 7 Hospital');
    await hospitalSection
      .getByLabel('Hospital email', { exact: true })
      .fill('hospital@phase7.test');

    const responsePromise = page.waitForResponse(
      (response) => response.url().includes('/register') && response.request().method() === 'POST',
    );
    await page.getByRole('button', { name: 'Create account' }).click();
    await responsePromise;

    // Success screen explicitly states no account exists yet.
    await expect(page.getByText('Check your inbox', { exact: true })).toBeVisible({
      timeout: 10_000,
    });
    await expect(page.getByText(/no account exists yet/i)).toBeVisible();
    await expect(page.getByText('admin@phase7.test')).toBeVisible();
  });
});

test.describe('Registration verification pages', () => {
  test('success page confirms account creation and links to sign in', async ({ page }) => {
    await page.goto('/verify-registration/success?tenant=phase7-hospital&email=admin@phase7.test', {
      waitUntil: 'domcontentloaded',
    });

    await expect(page.getByRole('heading', { name: 'Registration complete' })).toBeVisible();
    await expect(page.getByText('Your hospital account is ready')).toBeVisible();
    await expect(page.getByText(/free trial starts now/i)).toBeVisible();
    await expect(page.getByRole('link', { name: 'Continue to sign in' })).toHaveAttribute(
      'href',
      '/login',
    );
  });

  test('failed page explains an expired link', async ({ page }) => {
    await page.goto('/verify-registration/failed?reason=expired', {
      waitUntil: 'domcontentloaded',
    });

    await expect(page.getByRole('heading', { name: 'Verification failed' })).toBeVisible();
    await expect(page.getByText(/This verification link has expired/i)).toBeVisible();
    await expect(
      page.getByRole('link', { name: 'Start a new registration' }).first(),
    ).toHaveAttribute('href', '/register/hospital');
  });
});
