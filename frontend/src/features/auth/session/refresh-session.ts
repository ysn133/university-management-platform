import { authResponseSchema } from "../api/auth-contract";
import { clearTokenPair, getRefreshToken, storeTokenPair } from "./token-store";
import { env } from "@/shared/config/env";

let activeRefresh: Promise<void> | null = null;

async function performRefresh(): Promise<void> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    throw new Error("No refresh session is available");
  }

  const response = await fetch(`${env.apiBaseUrl}/api/v1/auth/refresh`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken }),
  });

  if (!response.ok) {
    clearTokenPair();
    throw new Error("The session has expired");
  }

  const session = authResponseSchema.parse(await response.json());
  storeTokenPair({
    accessToken: session.accessToken,
    refreshToken: session.refreshToken,
  });
}

export function refreshSession(): Promise<void> {
  if (!activeRefresh) {
    activeRefresh = performRefresh().finally(() => {
      activeRefresh = null;
    });
  }

  return activeRefresh;
}
