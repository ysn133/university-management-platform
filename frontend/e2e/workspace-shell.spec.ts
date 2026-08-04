import { expect, test, type Page } from "@playwright/test";

const universityId = "ba36991f-5063-414b-b0bc-2d2ac0777c50";
const establishmentId = "9849a830-b1a8-4f38-948d-2208aa6d6401";
const superAdminId = "69744941-a7c2-4802-8c97-19dfab23ca99";
const adminId = "0e3b754d-96d5-4898-9735-48671e62154d";

interface EstablishmentState {
  id: string;
  universityId: string;
  name: string;
  type: "FACULTY" | "SCHOOL" | "INSTITUTE";
  status: "ACTIVE" | "INACTIVE" | "ARCHIVED";
  createdAt: string;
  updatedAt: string;
}

interface SuperAdminState {
  id: string;
  accountId: string;
  establishmentId: string;
  email: string;
  role: "SUPER_ADMIN";
  status: "ACTIVE" | "LOCKED" | "DEACTIVATED" | "ARCHIVED";
  firstName: string;
  lastName: string;
  birthDate: string;
  cin: string | null;
  sex: "MALE" | "FEMALE";
  phoneNumber: string | null;
}

async function mockRootGovernanceApi(page: Page) {
  const establishments: EstablishmentState[] = [];
  const superAdmins: SuperAdminState[] = [];

  await page.route("**/api/v1/**", async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    const { pathname } = url;
    const method = request.method();

    if (method === "POST" && pathname === "/api/v1/auth/login") {
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
      });
      return;
    }

    if (method === "GET" && pathname === "/api/v1/university") {
      await route.fulfill({
        contentType: "application/json",
        json: { universityId, universityName: "Université Ibn Zohr" },
      });
      return;
    }

    if (method === "GET" && pathname === `/api/v1/university/${universityId}/establishments`) {
      await route.fulfill({ contentType: "application/json", json: establishments });
      return;
    }

    if (method === "POST" && pathname === "/api/v1/establishments") {
      const body = request.postDataJSON() as Pick<EstablishmentState, "name" | "type">;
      establishments.push({
        id: establishmentId,
        universityId,
        name: body.name,
        type: body.type,
        status: "ACTIVE",
        createdAt: "2026-08-03T18:00:00Z",
        updatedAt: "2026-08-03T18:00:00Z",
      });
      await route.fulfill({ contentType: "application/json", json: establishments[0] });
      return;
    }

    if (method === "GET" && pathname === `/api/v1/establishments/${establishmentId}`) {
      await route.fulfill({ contentType: "application/json", json: establishments[0] });
      return;
    }

    if (method === "PUT" && pathname === `/api/v1/establishments/${establishmentId}`) {
      Object.assign(establishments[0], request.postDataJSON(), { updatedAt: "2026-08-03T19:00:00Z" });
      await route.fulfill({ contentType: "application/json", json: establishments[0] });
      return;
    }

    if (method === "POST" && pathname === `/api/v1/establishments/${establishmentId}/deactivate`) {
      establishments[0].status = "INACTIVE";
      await route.fulfill({ contentType: "application/json", json: { success: true, message: "Establishment deactivated" } });
      return;
    }

    if (method === "GET" && pathname === `/api/v1/establishments/${establishmentId}/super-admins`) {
      await route.fulfill({ contentType: "application/json", json: superAdmins });
      return;
    }

    if (method === "GET" && pathname === `/api/v1/establishments/${establishmentId}/admins`) {
      await route.fulfill({ contentType: "application/json", json: [] });
      return;
    }

    if (method === "POST" && pathname === `/api/v1/establishments/${establishmentId}/super-admins`) {
      const body = request.postDataJSON() as {
        universityEmail: string;
        firstName: string;
        lastName: string;
        birth_date: string;
        cin?: string;
        sex: "MALE" | "FEMALE";
        phone_number?: string;
      };
      superAdmins.push({
        id: superAdminId,
        accountId: "42027403-01c5-4035-9c64-1e03733e6a58",
        establishmentId,
        email: body.universityEmail,
        role: "SUPER_ADMIN",
        status: "ACTIVE",
        firstName: body.firstName,
        lastName: body.lastName,
        birthDate: body.birth_date,
        cin: body.cin ?? null,
        sex: body.sex,
        phoneNumber: body.phone_number ?? null,
      });
      await route.fulfill({ contentType: "application/json", json: superAdmins[0] });
      return;
    }

    if (method === "PUT" && pathname === `/api/v1/super-admins/${superAdminId}`) {
      const body = request.postDataJSON() as {
        universityEmail: string;
        firstName: string;
        lastName: string;
        birth_date: string;
        cin?: string;
        sex: "MALE" | "FEMALE";
        phone_number?: string;
      };
      Object.assign(superAdmins[0], {
        email: body.universityEmail,
        firstName: body.firstName,
        lastName: body.lastName,
        birthDate: body.birth_date,
        cin: body.cin ?? null,
        sex: body.sex,
        phoneNumber: body.phone_number ?? null,
      });
      await route.fulfill({ contentType: "application/json", json: superAdmins[0] });
      return;
    }

    if (method === "POST" && pathname === `/api/v1/super-admins/${superAdminId}/password-reset`) {
      await route.fulfill({ contentType: "application/json", json: { success: true, message: "Password reset" } });
      return;
    }

    const lifecycleAction = pathname.match(new RegExp(`^/api/v1/super-admins/${superAdminId}/(lock|unlock|deactivate|activate|archive|restore)$`));
    if (method === "POST" && lifecycleAction) {
      const statuses = {
        lock: "LOCKED",
        unlock: "ACTIVE",
        deactivate: "DEACTIVATED",
        activate: "ACTIVE",
        archive: "ARCHIVED",
        restore: "DEACTIVATED",
      } as const;
      superAdmins[0].status = statuses[lifecycleAction[1] as keyof typeof statuses];
      await route.fulfill({ contentType: "application/json", json: { success: true, message: "Account updated" } });
      return;
    }

    await route.fulfill({ contentType: "application/json", json: { error: 404, message: `Unhandled ${method} ${pathname}` }, status: 404 });
  });
}

async function signIn(page: Page) {
  await page.goto("/");
  await expect(page).toHaveURL(/\/management\/login$/);
  await page.getByLabel("University email").fill("root@uiz.ac.ma");
  await page.getByLabel("Password").fill("change-me-now");
  await page.getByRole("button", { name: "Sign in" }).click();
}

async function mockSuperAdminWorkspaceApi(page: Page) {
  const admins: Array<Record<string, unknown>> = [];
  let grantedPermissions: string[] = [];

  await page.route("**/api/v1/**", async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    const { pathname } = url;
    const method = request.method();

    if (method === "POST" && pathname === "/api/v1/auth/login") {
      await route.fulfill({ contentType: "application/json", json: {
        userAccountId: "1247618c-0134-4a54-9466-c939f0c08d46",
        role: "SUPER_ADMIN",
        roleEntityId: "ec7ef366-2940-4db4-83bf-665854a8c243",
        establishmentId,
        universityEmail: "super-admin@uiz.ac.ma",
        firstName: "Salma",
        lastName: "Admin",
        accountStatus: "ACTIVE",
        accessToken: "access-token",
        refreshToken: "refresh-token",
      } });
      return;
    }

    if (method === "GET" && pathname === `/api/v1/establishments/${establishmentId}`) {
      await route.fulfill({ contentType: "application/json", json: {
        id: establishmentId,
        universityId,
        name: "Faculty of Sciences Agadir",
        type: "FACULTY",
        status: "ACTIVE",
        createdAt: "2026-08-03T18:00:00Z",
      } });
      return;
    }

    if (method === "GET" && pathname === `/api/v1/establishments/${establishmentId}/admins`) {
      await route.fulfill({ contentType: "application/json", json: admins });
      return;
    }

    if (method === "POST" && pathname === `/api/v1/establishments/${establishmentId}/admins`) {
      const body = request.postDataJSON() as Record<string, string>;
      admins.push({
        id: adminId,
        accountId: "224e2cdc-cb5f-4d90-b27c-1cbdd74e67b6",
        establishmentId,
        email: body.universityEmail,
        role: "ADMIN",
        status: "ACTIVE",
        firstName: body.firstName,
        lastName: body.lastName,
        birthDate: body.birth_date,
        cin: null,
        sex: body.sex,
        phoneNumber: body.phone_number ?? null,
      });
      await route.fulfill({ contentType: "application/json", json: {
        adminId: admins[0].id,
        userAccountId: admins[0].accountId,
        establishmentId,
        roleType: "ADMIN",
      } });
      return;
    }

    if (method === "GET" && pathname === `/api/v1/admins/${admins[0]?.id}`) {
      await route.fulfill({ contentType: "application/json", json: admins[0] });
      return;
    }

    if (method === "PUT" && pathname === `/api/v1/admins/${admins[0]?.id}`) {
      const body = request.postDataJSON() as Record<string, string>;
      Object.assign(admins[0], {
        email: body.universityEmail,
        firstName: body.firstName,
        lastName: body.lastName,
        birthDate: body.birth_date,
        cin: body.cin ?? null,
        sex: body.sex,
        phoneNumber: body.phone_number ?? null,
      });
      await route.fulfill({ contentType: "application/json", json: admins[0] });
      return;
    }

    if (method === "POST" && pathname === `/api/v1/admins/${admins[0]?.id}/password-reset`) {
      await route.fulfill({ contentType: "application/json", json: { success: true, message: "Password reset" } });
      return;
    }

    if (method === "GET" && pathname === "/api/v1/permissions") {
      await route.fulfill({ contentType: "application/json", json: [
        { id: "5c0fd080-b00a-438d-b4d8-991c973dd41a", code: "DEPARTMENT_VIEW", name: "View departments" },
        { id: "a7fe6347-fd01-4910-a697-5a9f49a69661", code: "DEPARTMENT_CREATE", name: "Create departments" },
      ] });
      return;
    }

    if (pathname === `/api/v1/admins/${admins[0]?.id}/permission-grants` && method === "GET") {
      await route.fulfill({ contentType: "application/json", json: { adminId: admins[0].id, establishmentId, permissions: grantedPermissions } });
      return;
    }

    if (pathname === `/api/v1/admins/${admins[0]?.id}/permission-grants` && method === "PUT") {
      grantedPermissions = (request.postDataJSON() as { permissions: string[] }).permissions;
      await route.fulfill({ contentType: "application/json", json: { adminId: admins[0].id, establishmentId, permissions: grantedPermissions } });
      return;
    }

    const lifecycleAction = pathname.match(new RegExp(`^/api/v1/admins/${admins[0]?.id}/(deactivate|activate)$`));
    if (method === "POST" && lifecycleAction) {
      admins[0].status = lifecycleAction[1] === "activate" ? "ACTIVE" : "DEACTIVATED";
      await route.fulfill({ contentType: "application/json", json: { success: true, message: "Account updated" } });
      return;
    }

    await route.fulfill({ contentType: "application/json", json: { error: 404, message: `Unhandled ${method} ${pathname}` }, status: 404 });
  });
}

test("signs in and opens the root governance dashboard", async ({ page }) => {
  await mockRootGovernanceApi(page);
  await signIn(page);

  await expect(page).toHaveURL(/\/management$/);
  await expect(page.getByRole("heading", { name: "Université Ibn Zohr" })).toBeVisible();
  await expect(page.getByText("No establishments have been created.")).toBeVisible();

  await page.getByRole("link", { name: "Security" }).click();
  await expect(page).toHaveURL(/\/management\/account\/password$/);
  await expect(page.getByRole("heading", { name: "Security & password" })).toBeVisible();
  await expect(page.getByText("root@uiz.ac.ma")).toBeVisible();
});

test("manages an establishment and its Super Admin", async ({ page }) => {
  await mockRootGovernanceApi(page);
  await signIn(page);

  await page.getByRole("button", { name: "New establishment" }).click();
  await page.getByLabel("Official name").fill("Faculty of Sciences Agadir");
  await page.getByLabel("Establishment type").selectOption("FACULTY");
  await page.getByRole("dialog").getByRole("button", { name: "Create establishment" }).click();

  await expect(page).toHaveURL(new RegExp(`/management/establishments/${establishmentId}$`));
  await expect(page.getByRole("heading", { name: "Faculty of Sciences Agadir" })).toBeVisible();

  await page.getByRole("button", { name: "Edit establishment" }).click();
  await expect(page.getByRole("dialog")).toBeVisible();
  await page.getByRole("dialog").getByRole("button", { name: "Cancel" }).click();

  await page.locator(".workspace-sidebar").getByRole("link", { name: "Super Admins" }).click();
  await expect(page).toHaveURL(new RegExp(`/management/establishments/${establishmentId}/super-admins$`));
  await page.getByRole("button", { name: "Add Super Admin" }).click();
  await page.getByLabel("First name").fill("Salma");
  await page.getByLabel("Last name").fill("Amrani");
  await page.getByLabel("University email").fill("salma.amrani@uiz.ac.ma");
  await page.getByLabel("Initial password").fill("temporary-password");
  await page.getByLabel("Birth date").fill("1990-05-14");
  await page.getByLabel("Sex").selectOption("FEMALE");
  await page.getByLabel("CIN").fill("AE123456");
  await page.getByLabel("Phone number").fill("0612345678");
  await page.getByRole("button", { name: "Create Super Admin" }).click();

  await expect(page.getByText("Salma Amrani")).toBeVisible();
  await page.getByRole("button", { name: "Edit", exact: true }).click();
  await page.getByLabel("Last name").fill("El Amrani");
  await page.getByRole("button", { name: "Save changes" }).click();
  await expect(page.getByText("Salma El Amrani")).toBeVisible();

  await page.getByRole("button", { name: "Reset password" }).click();
  await page.getByLabel("New temporary password").fill("another-password");
  await page.getByRole("dialog").getByRole("button", { name: "Reset password" }).click();
  await expect(page.getByRole("dialog")).not.toBeVisible();

  await page.getByRole("button", { name: "Lock" }).click();
  await page.getByRole("dialog").getByRole("button", { name: "Lock" }).click();
  await expect(page.getByText("LOCKED", { exact: true })).toBeVisible();

  await page.getByRole("button", { name: "Unlock" }).click();
  await page.getByRole("dialog").getByRole("button", { name: "Unlock" }).click();
  await expect(page.getByText("ACTIVE", { exact: true }).last()).toBeVisible();

  const superAdminRow = page.getByRole("row", { name: /Salma El Amrani/ });
  await superAdminRow.getByRole("button", { name: "Deactivate" }).click();
  await page.getByRole("dialog").getByRole("button", { name: "Deactivate" }).click();
  await expect(superAdminRow.getByText("DEACTIVATED", { exact: true })).toBeVisible();

  await superAdminRow.getByRole("button", { name: "Activate" }).click();
  await page.getByRole("dialog").getByRole("button", { name: "Activate" }).click();
  await expect(superAdminRow.getByText("ACTIVE", { exact: true })).toBeVisible();

  await superAdminRow.getByRole("button", { name: "Archive" }).click();
  await page.getByRole("dialog").getByRole("button", { name: "Archive" }).click();
  await expect(superAdminRow.getByText("ARCHIVED", { exact: true })).toBeVisible();

  await superAdminRow.getByRole("button", { name: "Restore" }).click();
  await page.getByRole("dialog").getByRole("button", { name: "Restore" }).click();
  await expect(superAdminRow.getByText("DEACTIVATED", { exact: true })).toBeVisible();

  await page.locator(".workspace-sidebar").getByRole("link", { name: "Overview" }).click();
  await page.locator(".context-status-control").getByRole("button", { name: "Deactivate" }).click();
  await page.getByRole("dialog").getByRole("button", { name: "Deactivate" }).click();
  await expect(page.locator(".context-summary-panel").getByText("INACTIVE", { exact: true })).toBeVisible();
  await expect(page.locator(".management-context-card").getByText("Faculty of Sciences Agadir")).toBeVisible();
});

test("Super Admin manages Admin accounts in the shared establishment workspace", async ({ page }) => {
  await mockSuperAdminWorkspaceApi(page);
  await page.goto("/management/login");
  await page.getByLabel("University email").fill("super-admin@uiz.ac.ma");
  await page.getByLabel("Password").fill("change-me-now");
  await page.getByRole("button", { name: "Sign in" }).click();

  await expect(page.getByRole("heading", { name: "Faculty of Sciences Agadir" })).toBeVisible();
  await page.locator(".workspace-sidebar").getByRole("link", { name: "Admins" }).click();
  await expect(page.getByRole("heading", { name: "Admins", exact: true })).toBeVisible();

  await page.getByRole("button", { name: "New Admin" }).click();
  await page.getByLabel("First name").fill("Omar");
  await page.getByLabel("Last name").fill("Alaoui");
  await page.getByLabel("University email").fill("omar.alaoui@uiz.ac.ma");
  await page.getByLabel("Initial password").fill("temporary-password");
  await page.getByLabel("Birth date").fill("1992-04-20");
  await page.getByRole("dialog").getByRole("button", { name: "Continue to permissions" }).click();
  await expect(page.getByText("Operational access")).toBeVisible();
  await page.getByText("View departments").click();
  await page.getByRole("button", { name: "Create with permissions" }).click();
  await expect(page.getByText("Omar Alaoui")).toBeVisible();

  const adminRow = page.getByRole("row", { name: /Omar Alaoui/ });
  await expect(adminRow.getByRole("button", { name: "Edit", exact: true })).toBeVisible();
  await expect(adminRow.getByRole("button", { name: "Permissions" })).toBeVisible();
  await expect(adminRow.getByRole("button", { name: "Reset password" })).toBeVisible();
  await expect(adminRow.getByRole("button", { name: "Deactivate" })).toBeVisible();
  await expect(adminRow.getByRole("button", { name: "Archive" })).toBeVisible();

  await page.locator(".resource-name--link").filter({ hasText: "Omar Alaoui" }).click();
  await expect(page).toHaveURL(new RegExp(`/management/admins/${adminId}$`));
  await expect(page.getByRole("heading", { name: "Omar Alaoui" })).toBeVisible();

  await page.getByRole("button", { name: "Edit profile" }).click();
  await page.getByLabel("Last name").fill("El Alaoui");
  await page.getByRole("button", { name: "Save changes" }).click();
  await expect(page.getByRole("heading", { name: "Omar El Alaoui" })).toBeVisible();

  await page.getByRole("button", { name: /Permissions/ }).click();
  await page.getByRole("button", { name: "Edit permissions" }).click();
  await expect(page.getByLabel("View departments")).toBeChecked();
  await page.getByText("Create departments").click();
  await page.getByRole("button", { name: "Save permissions" }).click();
  await expect(page.getByRole("dialog")).not.toBeVisible();

  await page.getByRole("button", { name: "Overview", exact: true }).click();
  await page.getByRole("button", { name: "Deactivate" }).click();
  await page.getByRole("dialog").getByRole("button", { name: "Deactivate" }).click();
  await expect(page.getByText("DEACTIVATED", { exact: true })).toBeVisible();
  await page.getByRole("button", { name: "Activate" }).click();
  await page.getByRole("dialog").getByRole("button", { name: "Activate" }).click();
  await expect(page.getByText("ACTIVE", { exact: true })).toBeVisible();
});
