import { chromium } from 'playwright';
import { writeFileSync } from 'fs';

const BASE_URL = 'http://localhost:3000';

const browser = await chromium.launch({ args: ['--no-sandbox'] });

async function takeScreenshot(width, label) {
  const context = await browser.newContext({
    viewport: { width, height: 900 },
    deviceScaleFactor: 2,
  });
  const page = await context.newPage();

  // Block fonts to speed up
  await page.route('**/*.woff2', (r) => r.abort());

  await page.goto(BASE_URL, { waitUntil: 'networkidle', timeout: 30000 });

  // Wait for the hero heading to appear
  await page.waitForSelector('h1', { timeout: 15000 });

  // Extra settle time for fonts/transitions
  await page.waitForTimeout(1000);

  const path = `screenshot-${label}.png`;
  await page.screenshot({ path, fullPage: true });
  console.log(`Saved ${path}`);

  await context.close();
}

try {
  await takeScreenshot(1440, 'desktop');
  await takeScreenshot(375, 'mobile');
  console.log('Done — both screenshots saved.');
} finally {
  await browser.close();
}
