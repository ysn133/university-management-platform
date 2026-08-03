import { beforeEach, describe, expect, it, vi } from "vitest";
import { refreshSession } from "./refresh-session";
import { clearTokenPair, getAccessToken, getRefreshToken, storeTokenPair } from "./token-store";

const refreshedSession = {
  userAccountId: "bc9ab3ca-7802-4d86-b502-66b87f25f70e",
  role: "ROOT_SUPER_ADMIN",
  roleEntityId: "6308849f-64be-46ec-b25c-58c725e451a1",
  establishmentId: null,
  universityEmail: "root@uiz.ac.ma",
  firstName: "Root",
  lastName: "Admin",
  accountStatus: "ACTIVE",
  accessToken: "new-access-token",
  refreshToken: "new-refresh-token",
};

describe("refresh session", () => {
  beforeEach(() => {
    clearTokenPair();
    vi.restoreAllMocks();
  });

  it("rotates tokens once when concurrent requests need a refresh", async () => {
    storeTokenPair({ accessToken: "expired", refreshToken: "old-refresh-token" });
    const fetchMock = vi
      .spyOn(globalThis, "fetch")
      .mockResolvedValue(new Response(JSON.stringify(refreshedSession), { status: 200 }));

    await Promise.all([refreshSession(), refreshSession()]);

    expect(fetchMock).toHaveBeenCalledOnce();
    expect(getAccessToken()).toBe("new-access-token");
    expect(getRefreshToken()).toBe("new-refresh-token");
  });

  it("clears the session when refresh is rejected", async () => {
    storeTokenPair({ accessToken: "expired", refreshToken: "invalid-refresh-token" });
    vi.spyOn(globalThis, "fetch").mockResolvedValue(new Response(null, { status: 401 }));

    await expect(refreshSession()).rejects.toThrow("session has expired");
    expect(getAccessToken()).toBeNull();
    expect(getRefreshToken()).toBeNull();
  });
});
