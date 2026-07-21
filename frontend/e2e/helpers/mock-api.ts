import type { Page, Route } from '@playwright/test';

/**
 * Fulfills browser calls to the Spring Boot API (including CORS preflight).
 */
export async function mockJson(
  page: Page,
  urlGlob: string,
  body: unknown,
  status = 200,
): Promise<void> {
  await page.route(urlGlob, async (route: Route) => {
    if (route.request().method() === 'OPTIONS') {
      await route.fulfill({
        status: 204,
        headers: {
          'Access-Control-Allow-Origin': '*',
          'Access-Control-Allow-Methods': 'GET,POST,PUT,DELETE,OPTIONS',
          'Access-Control-Allow-Headers': '*, Authorization, Content-Type',
        },
      });
      return;
    }

    await route.fulfill({
      status,
      contentType: 'application/json',
      headers: {
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Headers': '*, Authorization, Content-Type',
      },
      body: JSON.stringify(body),
    });
  });
}
