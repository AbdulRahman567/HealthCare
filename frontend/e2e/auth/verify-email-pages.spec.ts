import { expect, test } from '@playwright/test';

import { mockJson } from '../helpers/mock-api';

test.describe('Verify email pages', () => {
  test('the success page confirms verification and links to sign in', async ({ page }) => {
    await page.goto('/verify-email/success', { waitUntil: 'domcontentloaded', timeout: 60_000 });

    await expect(page.getByRole('heading', { name: 'Email verified' })).toBeVisible();
    await expect(page.getByText('Verification complete')).toBeVisible();
    await expect(page.getByRole('link', { name: 'Continue to sign in' })).toHaveAttribute(
      'href',
      '/login',
    );
  });

  test('the failed page explains an expired link and offers a resend link', async ({ page }) => {
    await page.goto('/verify-email/failed?reason=expired', { waitUntil: 'domcontentloaded' });

    await expect(page.getByRole('heading', { name: 'Verification failed' })).toBeVisible();
    await expect(page.getByText('Unable to verify email')).toBeVisible();
    await expect(
      page.getByText('This verification link has expired. Request a new one to continue.'),
    ).toBeVisible();
    await expect(
      page.getByRole('link', { name: 'Request a new verification link' }),
    ).toHaveAttribute('href', '/resend-verification');
  });

  test('the failed page explains an invalid link', async ({ page }) => {
    await page.goto('/verify-email/failed?reason=invalid', { waitUntil: 'domcontentloaded' });

    await expect(
      page.getByText('This verification link is invalid or has already been used.'),
    ).toBeVisible();
  });

  test('the failed page explains a missing token', async ({ page }) => {
    await page.goto('/verify-email/failed?reason=missing', { waitUntil: 'domcontentloaded' });

    await expect(
      page.getByText('This page was opened without a verification token.'),
    ).toBeVisible();
  });

  test('the verification handler redirects to the failed page when no token is present', async ({
    page,
  }) => {
    await page.goto('/verify-email', { waitUntil: 'domcontentloaded' });

    await expect(page).toHaveURL(/\/verify-email\/failed\?reason=missing$/, { timeout: 15_000 });
  });

  test('the verification handler redirects to the success page on a valid token', async ({
    page,
  }) => {
    await mockJson(page, '**/auth/verify-email', {
      success: true,
      message: 'Email verified',
      timestamp: new Date().toISOString(),
      data: null,
    });

    await page.goto(`/verify-email?token=${'a'.repeat(32)}`, { waitUntil: 'domcontentloaded' });

    await expect(page).toHaveURL(/\/verify-email\/success$/, { timeout: 15_000 });
  });
});
