const { chromium } = require("playwright");

(async () => {
  const browser = await chromium.launch();
  const dir = "audit-screenshots";

  try {
    const context = await browser.newContext({ viewport: { width: 1280, height: 800 } });
    const page = await context.newPage();

    // Step 1: Register hospital page (admin account step)
    await page.goto("http://localhost:3000/register/hospital", { waitUntil: "networkidle", timeout: 30000 });
    await page.waitForTimeout(1000);
    await page.screenshot({ path: `${dir}/phase2-step1-admin-account.png`, fullPage: true });
    console.log(`  OK  step1-admin-account  (title: "${await page.title()}")`);

    // Fill in Step 1 and submit to get to Step 2
    await page.fill("#adminFirstName", "Demo");
    await page.fill("#adminLastName", "Admin");
    await page.fill("#adminEmail", "demo-step2@screenshot.com");
    await page.fill("#adminPassword", "Str0ng!Pass#2024");
    await page.fill('input[id="adminPhone"]', "+1-555-9999");
    await page.click('button[type="submit"]');

    // Wait for Step 2 to appear (navigate to hospital setup)
    await page.waitForTimeout(3000);
    await page.waitForSelector("#hospitalName", { timeout: 15000 }).catch(() => {});
    await page.screenshot({ path: `${dir}/phase2-step2-hospital-setup.png`, fullPage: true });
    console.log(`  OK  step2-hospital-setup`);

    // Fill in Step 2
    await page.fill("#hospitalName", "Demo Hospital");
    await page.fill("#hospitalEmail", "demo@hospital.com");
    await page.fill('input[id="hospitalPhone"]', "+1-555-8888");
    await page.fill("#hospitalAddress", "123 Demo Street");
    await page.click('button[type="submit"]');

    // Wait for Step 3 (review)
    await page.waitForTimeout(2000);
    await page.waitForSelector('text=Review and confirm', { timeout: 10000 }).catch(() => {});
    await page.screenshot({ path: `${dir}/phase2-step3-confirm.png`, fullPage: true });
    console.log(`  OK  step3-confirm`);

    await context.close();
  } catch (e) {
    console.log(`  ERR: ${e.message}`);
  }

  await browser.close();
  console.log("Done.");
})();
