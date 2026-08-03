import type { components } from "@/shared/api/generated/schema";
import { apiClient } from "@/shared/api/client/api-client";
import { apiRequestError } from "@/shared/api/client/ApiRequestError";
import { authResponseSchema, currentUserResponseSchema, toAuthenticatedUser } from "./auth-contract";
import { refreshSession } from "../session/refresh-session";
import {
  clearTokenPair,
  getAccessToken,
  getRefreshToken,
  storeTokenPair,
} from "../session/token-store";
import type { AuthenticatedUser } from "../model/auth-types";
import { env } from "@/shared/config/env";

export type LoginRequest = components["schemas"]["LoginRequest"];
export type ChangePasswordRequest = components["schemas"]["ChangePasswordRequest"];

export async function login(request: LoginRequest): Promise<AuthenticatedUser> {
  const result = await apiClient.POST("/api/v1/auth/login", { body: request });
  if (!result.response.ok || !result.data) {
    throw apiRequestError(result.response, result.error);
  }

  const session = authResponseSchema.parse(result.data);
  storeTokenPair({
    accessToken: session.accessToken,
    refreshToken: session.refreshToken,
  });
  return toAuthenticatedUser(session);
}

export async function getCurrentUser(): Promise<AuthenticatedUser> {
  const result = await apiClient.GET("/api/v1/auth/me");
  if (!result.response.ok || !result.data) {
    throw apiRequestError(result.response, result.error);
  }

  return toAuthenticatedUser(currentUserResponseSchema.parse(result.data));
}

export async function restoreSession(): Promise<AuthenticatedUser | null> {
  if (!getRefreshToken()) {
    return null;
  }

  await refreshSession();
  return getCurrentUser();
}

async function sendLogout(): Promise<Response> {
  const accessToken = getAccessToken();
  const refreshToken = getRefreshToken();
  if (!accessToken || !refreshToken) {
    return new Response(null, { status: 204 });
  }

  return fetch(`${env.apiBaseUrl}/api/v1/auth/logout`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${accessToken}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ refreshToken }),
  });
}

export async function logout(): Promise<void> {
  try {
    let response = await sendLogout();
    if (response.status === 401 && getRefreshToken()) {
      await refreshSession();
      response = await sendLogout();
    }

    if (!response.ok && response.status !== 204) {
      throw apiRequestError(response, undefined);
    }
  } finally {
    clearTokenPair();
  }
}

export async function changePassword(request: ChangePasswordRequest): Promise<void> {
  const result = await apiClient.POST("/api/v1/auth/change-password", { body: request });
  if (!result.response.ok) {
    throw apiRequestError(result.response, result.error);
  }
}
