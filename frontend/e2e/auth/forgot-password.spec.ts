import { expect, test } from '@playwright/test';

import { mockJson } from '../helpers/mock-api';

test.describe('Forgot password page', () => {
  test('loads with the work email field and a link back to sign in', async ({ page }) => {
    await page.goto('/forgot-password', { waitUntil: 'domcontentloaded' });

    await expect(page.getByRole('heading', { name: 'Forgot password' })).toBeVisible();
    await expect(page.getByLabel('Work email')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Send reset link' })).toBeVisible();
    await expect(page.getByRole('link', { name: 'Back to sign in' })).toHaveAttribute(
      'href',
      '/login',
    );
  });

  test('shows a validation error when submitting an empty form', async ({ page }) => {
    await page.goto('/forgot-password', { waitUntil: 'domcontentloaded' });

    await page.getByRole('button', { name: 'Send reset link' }).click();

    await expect(page.getByText('Email is required')).toBeVisible();
  });

  test('shows a generic success message after a valid submission', async ({ page }) => {
    await mockJson(page, '**/auth/forgot-password', {
      success: true,
      message: 'If an account exists, a reset link was sent.',
      timestamp: new Date().toISOString(),
      data: null,
    });

    await page.goto('/forgot-password', { waitUntil: 'domcontentloaded' });
    await page.getByLabel('Work email').fill('user@hospital.com');

    const responsePromise = page.waitForResponse(
      (response) =>
        response.url().includes('/auth/forgot-password') && response.request().method() === 'POST',
    );
    await page.getByRole('button', { name: 'Send reset link' }).click();
    await responsePromise;

    await expect(page.getByText('Check your email', { exact: true })).toBeVisible({
      timeout: 10_000,
    });
    await expect(page.getByLabel('Work email')).toHaveCount(0);
  });

  test('shows an error message when the request fails', async ({ page }) => {
    await mockJson(
      page,
      '**/auth/forgot-password',
      { success: false, message: 'Something went wrong' },
      500,
    );

    await page.goto('/forgot-password', { waitUntil: 'domcontentloaded' });
    await page.getByLabel('Work email').fill('user@hospital.com');
    await page.getByRole('button', { name: 'Send reset link' }).click();

    await expect(page.getByText('Request failed', { exact: true })).toBeVisible();
  });
});
