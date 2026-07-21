import { expect, test } from '@playwright/test';

test.describe('Protected routes', () => {
  test('visiting /app without a session redirects to /login', async ({ page }) => {
    await page.goto('/app', { waitUntil: 'domcontentloaded' });

    await expect(page).toHaveURL(/\/login/);
    await expect(page.getByRole('heading', { name: 'Sign in' })).toBeVisible();
  });

  test('preserves the original destination as a "next" redirect param', async ({ page }) => {
    await page.goto('/app/profile', { waitUntil: 'domcontentloaded' });

    await expect(page).toHaveURL(/\/login\?next=%2Fapp%2Fprofile/);
  });

  test('visiting a nested protected route without a session also redirects to /login', async ({
    page,
  }) => {
    await page.goto('/app/settings', { waitUntil: 'domcontentloaded' });

    await expect(page).toHaveURL(/\/login\?next=/);
  });
});
