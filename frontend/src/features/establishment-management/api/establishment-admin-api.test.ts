import { afterEach, describe, expect, it, vi } from "vitest";
import { getPermissionCatalog } from "./establishment-admin-api";

describe("getPermissionCatalog", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("uses the permission code as the frontend identity", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(new Response(JSON.stringify([
      {
        id: "00000000-0000-0000-0000-000000000101",
        code: "DEPARTMENT_VIEW",
        name: "View departments",
      },
    ]), {
      status: 200,
      headers: { "Content-Type": "application/json" },
    })));

    await expect(getPermissionCatalog()).resolves.toEqual([
      { code: "DEPARTMENT_VIEW", name: "View departments" },
    ]);
  });
});
