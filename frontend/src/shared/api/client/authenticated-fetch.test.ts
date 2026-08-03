import { beforeEach, describe, expect, it, vi } from "vitest";
import { clearTokenPair, storeTokenPair } from "@/features/auth/session/token-store";
import { authenticatedFetch } from "./authenticated-fetch";

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

describe("authenticated fetch", () => {
  beforeEach(() => {
    clearTokenPair();
    vi.restoreAllMocks();
  });

  it("refreshes an expired access token and retries the original request", async () => {
    storeTokenPair({ accessToken: "expired-token", refreshToken: "refresh-token" });
    const fetchMock = vi
      .spyOn(globalThis, "fetch")
      .mockResolvedValueOnce(new Response(null, { status: 401 }))
      .mockResolvedValueOnce(new Response(JSON.stringify(refreshedSession), { status: 200 }))
      .mockResolvedValueOnce(new Response(JSON.stringify({ value: "protected" }), { status: 200 }));

    const response = await authenticatedFetch("http://localhost:8080/api/v1/protected");

    expect(response.status).toBe(200);
    expect(fetchMock).toHaveBeenCalledTimes(3);
    const retriedRequest = fetchMock.mock.calls[2][0] as Request;
    expect(retriedRequest.headers.get("Authorization")).toBe("Bearer new-access-token");
  });
});
