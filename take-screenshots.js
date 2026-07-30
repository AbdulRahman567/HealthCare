const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({
    viewport: { width: 1280, height: 900 },
  });
  const page = await context.newPage();

  const baseUrl = 'http://localhost:3002';

  // 1. Homepage with Pricing nav link
  await page.goto(baseUrl, { waitUntil: 'networkidle' });
  await page.waitForTimeout(1000);
  await page.screenshot({ path: 'audit-screenshots/phase3-homepage.png', fullPage: true });
  console.log('✓ Homepage screenshot saved');

  // 2. Pricing page
  await page.goto(`${baseUrl}/pricing`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(2000);
  await page.screenshot({ path: 'audit-screenshots/phase3-pricing-page.png', fullPage: true });
  console.log('✓ Pricing page screenshot saved');

  // 3. Register hospital (default plan - BASIC)
  await page.goto(`${baseUrl}/register/hospital`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(1000);
  await page.screenshot({ path: 'audit-screenshots/phase3-register-default.png', fullPage: true });
  console.log('✓ Register (default) screenshot saved');

  // 4. Register hospital with Premium plan (from pricing page)
  await page.goto(`${baseUrl}/register/hospital?plan=PREMIUM`, { waitUntil: 'networkidle' });
  await page.waitForTimeout(1000);
  await page.screenshot({ path: 'audit-screenshots/phase3-register-premium.png', fullPage: true });
  console.log('✓ Register (Premium) screenshot saved');

  await browser.close();
  console.log('\nAll screenshots captured successfully!');
})();
