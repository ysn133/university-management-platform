import { expect, test } from "@playwright/test";

test("opens the management workspace from the application root", async ({ page }) => {
  await page.goto("/");

  await expect(page).toHaveURL(/\/management$/);
  await expect(
    page.getByRole("heading", { name: "University operations, without duplicated dashboards." }),
  ).toBeVisible();
});
