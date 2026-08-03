import { expect, test } from "@playwright/test";

test("signs in and opens the management workspace", async ({ page }) => {
  await page.route("**/api/v1/auth/login", async (route) => {
    await route.fulfill({
      contentType: "application/json",
      json: {
        userAccountId: "bc9ab3ca-7802-4d86-b502-66b87f25f70e",
        role: "ROOT_SUPER_ADMIN",
        roleEntityId: "6308849f-64be-46ec-b25c-58c725e451a1",
        establishmentId: null,
        universityEmail: "root@uiz.ac.ma",
        firstName: "Root",
        lastName: "Admin",
        accountStatus: "ACTIVE",
        accessToken: "access-token",
        refreshToken: "refresh-token",
      },
      status: 200,
    });
  });

  await page.goto("/");

  await expect(page).toHaveURL(/\/management\/login$/);
  await page.getByLabel("University email").fill("root@uiz.ac.ma");
  await page.getByLabel("Password").fill("change-me-now");
  await page.getByRole("button", { name: "Sign in" }).click();

  await expect(page).toHaveURL(/\/management$/);
  await expect(
    page.getByRole("heading", { name: "University operations, without duplicated dashboards." }),
  ).toBeVisible();
});
