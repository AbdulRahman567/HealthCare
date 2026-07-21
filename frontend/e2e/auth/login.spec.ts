import { expect, test } from '@playwright/test';

import { mockJson } from '../helpers/mock-api';

const sampleUser = {
  id: 'user-1',
  tenantId: 'tenant-1',
  firstName: 'Ada',
  lastName: 'Lovelace',
  email: 'ada@hospital.com',
  phone: null,
  emailVerified: true,
  emailVerifiedAt: null,
  status: 'ACTIVE',
  roles: ['ADMIN'],
  permissions: [],
  lastLoginAt: null,
  createdAt: new Date().toISOString(),
};

test.describe('Login page', () => {
  test('loads with the sign-in form and a link to register a hospital', async ({ page }) => {
    await page.goto('/login', { waitUntil: 'domcontentloaded' });

    await expect(page.getByRole('heading', { name: 'Sign in' })).toBeVisible();
    await expect(page.getByLabel('Email')).toBeVisible();
    await expect(page.getByLabel('Password')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Sign in' })).toBeVisible();
    await expect(page.getByRole('link', { name: 'Register your hospital' })).toHaveAttribute(
      'href',
      '/register/hospital',
    );
  });

  test('shows validation errors when submitting an empty form', async ({ page }) => {
    await page.goto('/login', { waitUntil: 'domcontentloaded' });

    await page.getByRole('button', { name: 'Sign in' }).click();

    await expect(page.getByText('Email is required')).toBeVisible();
    await expect(page.getByText('Password is required')).toBeVisible();
  });

  test('shows a validation error for a malformed email', async ({ page }) => {
    await page.goto('/login', { waitUntil: 'domcontentloaded' });

    await page.getByLabel('Email').fill('not-an-email');
    await page.getByLabel('Password').fill('some-password');
    await page.getByRole('button', { name: 'Sign in' }).click();

    await expect(
      page.getByText(/valid email|email address/i).first(),
    ).toBeVisible({ timeout: 10_000 });
  });

  test('links to the forgot password page', async ({ page }) => {
    await page.goto('/login', { waitUntil: 'domcontentloaded' });

    await page.getByRole('link', { name: 'Forgot password?' }).click();

    await expect(page).toHaveURL(/\/forgot-password$/);
    await expect(page.getByRole('heading', { name: 'Forgot password' })).toBeVisible();
  });

  test('signs in and redirects to /app on valid credentials', async ({ page }) => {
    await mockJson(page, '**/auth/login', {
      success: true,
      message: 'Signed in',
      timestamp: new Date().toISOString(),
      data: {
        accessToken: 'access-token',
        refreshToken: 'refresh-token',
        tokenType: 'Bearer',
        expiresInSeconds: 900,
        refreshExpiresInSeconds: 3600,
        user: sampleUser,
      },
    });
    await mockJson(page, '**/auth/profile', {
      success: true,
      message: 'OK',
      timestamp: new Date().toISOString(),
      data: sampleUser,
    });
    await mockJson(page, '**/auth/refresh-token', {
      success: true,
      message: 'OK',
      timestamp: new Date().toISOString(),
      data: {
        accessToken: 'access-token',
        refreshToken: 'refresh-token',
        tokenType: 'Bearer',
        expiresInSeconds: 900,
        refreshExpiresInSeconds: 3600,
        user: sampleUser,
      },
    });

    await page.goto('/login', { waitUntil: 'domcontentloaded' });
    await page.getByLabel('Email').fill('ada@hospital.com');
    await page.getByLabel('Password').fill('CorrectPass1!');

    const loginResponse = page.waitForResponse(
      (response) => response.url().includes('/auth/login') && response.request().method() === 'POST',
    );
    await page.getByRole('button', { name: 'Sign in' }).click();
    const response = await loginResponse;
    expect(response.status()).toBe(200);

    await expect(page.getByText('Signed in successfully')).toBeVisible({ timeout: 10_000 });
    await expect.poll(async () => page.url(), { timeout: 20_000 }).toMatch(/\/app(?:\/)?$/);
  });

  test('shows an error message on invalid credentials', async ({ page }) => {
    await mockJson(
      page,
      '**/auth/login',
      {
        success: false,
        message: 'Invalid email or password',
        errorCode: 'AUTH_INVALID_CREDENTIALS',
      },
      401,
    );

    await page.goto('/login', { waitUntil: 'domcontentloaded' });
    await page.getByLabel('Email').fill('ada@hospital.com');
    await page.getByLabel('Password').fill('WrongPassword1!');
    await page.getByRole('button', { name: 'Sign in' }).click();

    await expect(page.getByText('Invalid email or password').first()).toBeVisible();
    await expect(page).toHaveURL(/\/login$/);
  });
});
