import { expect, test } from '@playwright/test';

/**
 * Phase 4 — Marketing landing page verification.
 *
 * Asserts, via DOM only (no screenshot reading):
 *   - Each homepage section (Hero, Trust, Features, How It Works, Pricing
 *     teaser, Final CTA) renders with expected text/elements.
 *   - Sections appear in the correct vertical order.
 *   - SiteHeader and SiteFooter render and their links navigate correctly.
 *   - The mobile nav menu toggle exists and toggles the mobile nav.
 *   - HTTP 200 on the homepage and zero console/page errors.
 * All checks run at both desktop and mobile viewport sizes.
 */

const DESKTOP = { width: 1440, height: 900 };
const MOBILE = { width: 375, height: 812 };

// Unique text markers, one per section, in page order.
const SECTION_MARKERS: Array<{ name: string; marker: string }> = [
  { name: 'Hero', marker: 'The operating system' },
  { name: 'Trust', marker: 'Trusted worldwide' },
  { name: 'Features', marker: 'One platform, designed for healthcare' },
  { name: 'How It Works', marker: 'Up and running in 3 simple steps' },
  { name: 'Pricing teaser', marker: 'Start for free, scale as you grow' },
  { name: 'Final CTA', marker: 'Ready to transform your hospital operations?' },
];

/** Runs the full homepage verification for a given viewport. */
async function verifyHomepage(
  page: import('@playwright/test').Page,
  viewport: { width: number; height: number },
  isMobile: boolean,
): Promise<void> {
  const consoleErrors: string[] = [];
  const pageErrors: string[] = [];
  page.on('console', (msg) => {
    if (msg.type() === 'error') consoleErrors.push(msg.text());
  });
  page.on('pageerror', (err) => pageErrors.push(err.message));

  await page.setViewportSize(viewport);

  const response = await page.goto('/', { waitUntil: 'networkidle' });
  expect(response?.status(), 'homepage should return HTTP 200').toBe(200);

  await page.waitForSelector('main h1', { timeout: 15_000 });

  // 1. Each section renders with expected text.
  for (const { name, marker } of SECTION_MARKERS) {
    await expect(
      page.getByText(marker, { exact: false }).first(),
      `section '${name}' should render its marker`,
    ).toBeVisible();
  }

  // 2. Sections appear in the correct order (index of each marker is ascending).
  const mainText = await page.locator('main').innerText();
  const indices = SECTION_MARKERS.map(({ marker }) => mainText.indexOf(marker));
  for (let i = 1; i < indices.length; i += 1) {
    expect(
      indices[i],
      `section '${SECTION_MARKERS[i].name}' should come after '${SECTION_MARKERS[i - 1].name}'`,
    ).toBeGreaterThan(indices[i - 1]);
  }

  // 3a. SiteHeader renders: logo, desktop nav (or mobile toggle).
  const header = page.locator('header');
  await expect(header).toBeVisible();
  await expect(header.getByText('Healthcare HMS').first()).toBeVisible();

  const logoLink = header.locator('a[href="/"]').first();
  await expect(logoLink).toBeVisible();

  // 3b. Mobile nav menu toggle exists and works on mobile; desktop nav hidden.
  const hamburger = header.getByRole('button', { name: 'Open menu' });
  if (isMobile) {
    await expect(hamburger).toBeVisible();
    await expect(header.getByRole('navigation').first()).toBeHidden();

    await hamburger.click();
    const mobileNav = header.getByRole('button', { name: 'Close menu' }).locator('..');
    await expect(
      header.getByRole('link', { name: 'Pricing' }).last(),
      'mobile nav should expose the Pricing link',
    ).toBeVisible();

    // Toggle closed again.
    await header.getByRole('button', { name: 'Close menu' }).click();
    await expect(hamburger).toBeVisible();
  } else {
    await expect(hamburger).toBeHidden();
    await expect(header.getByRole('link', { name: 'Pricing' })).toBeVisible();
    await expect(header.getByRole('link', { name: 'Register hospital' })).toBeVisible();
  }

  // 3c. SiteFooter renders with expected links.
  const footer = page.locator('footer');
  await expect(footer).toBeVisible();
  await expect(footer.getByText('The operating system for modern hospitals.')).toBeVisible();
  for (const label of ['Pricing', 'Sign in', 'Register hospital']) {
    await expect(footer.getByRole('link', { name: label })).toBeVisible();
  }

  // 4. Header navigation links navigate correctly (desktop nav vs mobile menu).
  if (isMobile) {
    await hamburger.click();
  }
  await header.getByRole('link', { name: 'Pricing' }).first().click();
  await page.waitForURL('**/pricing');
  expect(page.url()).toContain('/pricing');
  await page.goto('/', { waitUntil: 'networkidle' });

  // 5. Zero console / page errors.
  expect(consoleErrors, `no console errors (got: ${consoleErrors.join(' | ')})`).toEqual([]);
  expect(pageErrors, `no page errors (got: ${pageErrors.join(' | ')})`).toEqual([]);
}

test.describe('Marketing landing page', () => {
  test('renders all sections, header/footer, links, no errors — desktop', async ({ page }) => {
    await verifyHomepage(page, DESKTOP, false);
  });

  test('renders all sections, mobile nav toggle, no errors — mobile', async ({ page }) => {
    await verifyHomepage(page, MOBILE, true);
  });
});
