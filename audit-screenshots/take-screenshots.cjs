const { chromium } = require("playwright");

(async () => {
  const browser = await chromium.launch();
  const dir = "audit-screenshots";
  const urls = [
    { name: "homepage", url: "http://localhost:3000" },
    { name: "login", url: "http://localhost:3000/login" },
    { name: "register-hospital", url: "http://localhost:3000/register/hospital" },
  ];

  for (const u of urls) {
    try {
      const context = await browser.newContext({ viewport: { width: 1280, height: 800 } });
      const page = await context.newPage();
      await page.goto(u.url, { waitUntil: "networkidle", timeout: 30000 });
      await page.screenshot({ path: `${dir}/${u.name}.png`, fullPage: true });
      console.log(`  OK  ${u.name}  (title: "${await page.title()}")`);
      await context.close();
    } catch (e) {
      console.log(`  ERR ${u.name}: ${e.message.slice(0, 120)}`);
    }
  }

  await browser.close();
  console.log("Done.");
})();
