import { expect, test } from '@playwright/test';

import { mockJson } from '../helpers/mock-api';

test.describe('Resend verification page', () => {
  test('loads with the work email field and a link to sign in', async ({ page }) => {
    await page.goto('/resend-verification', { waitUntil: 'domcontentloaded' });

    await expect(page.getByRole('heading', { name: 'Resend verification' })).toBeVisible();
    await expect(page.getByLabel('Work email')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Resend verification link' })).toBeVisible();
    await expect(page.getByRole('link', { name: 'Sign in' })).toHaveAttribute('href', '/login');
  });

  test('shows a validation error when submitting an empty form', async ({ page }) => {
    await page.goto('/resend-verification', { waitUntil: 'domcontentloaded' });

    await page.getByRole('button', { name: 'Resend verification link' }).click();

    await expect(page.getByText('Email is required')).toBeVisible();
  });

  test('shows a generic success message after a valid submission', async ({ page }) => {
    await mockJson(page, '**/auth/resend-verification', {
      success: true,
      message: 'If unverified, a new link was sent.',
      timestamp: new Date().toISOString(),
      data: null,
    });

    await page.goto('/resend-verification', { waitUntil: 'domcontentloaded' });
    await page.getByLabel('Work email').fill('user@hospital.com');

    const responsePromise = page.waitForResponse(
      (response) =>
        response.url().includes('/auth/resend-verification') &&
        response.request().method() === 'POST',
    );
    await page.getByRole('button', { name: 'Resend verification link' }).click();
    await responsePromise;

    await expect(page.getByText('Check your email', { exact: true })).toBeVisible({
      timeout: 10_000,
    });
  });

  test('shows an error message when the request fails', async ({ page }) => {
    await mockJson(
      page,
      '**/auth/resend-verification',
      { success: false, message: 'Something went wrong' },
      500,
    );

    await page.goto('/resend-verification', { waitUntil: 'domcontentloaded' });
    await page.getByLabel('Work email').fill('user@hospital.com');
    await page.getByRole('button', { name: 'Resend verification link' }).click();

    await expect(page.getByText('Request failed', { exact: true })).toBeVisible();
  });
});
