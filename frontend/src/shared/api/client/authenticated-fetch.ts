import { refreshSession } from "@/features/auth/session/refresh-session";
import { clearTokenPair, getAccessToken } from "@/features/auth/session/token-store";

const refreshExcludedPaths = [
  "/api/v1/auth/login",
  "/api/v1/auth/refresh",
  "/api/v1/auth/logout",
];

function withAccessToken(request: Request, token: string | null): Request {
  if (!token) {
    return request;
  }

  const headers = new Headers(request.headers);
  headers.set("Authorization", `Bearer ${token}`);
  return new Request(request, { headers });
}

export const authenticatedFetch: typeof fetch = async (input, init) => {
  const request = input instanceof Request ? input : new Request(input, init);
  const retryRequest = request.clone();
  const response = await fetch(withAccessToken(request, getAccessToken()));
  const canRefresh = !refreshExcludedPaths.some((path) => request.url.endsWith(path));

  if (response.status !== 401 || !canRefresh) {
    return response;
  }

  try {
    await refreshSession();
    return fetch(withAccessToken(retryRequest, getAccessToken()));
  } catch {
    clearTokenPair();
    return response;
  }
};
