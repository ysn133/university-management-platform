import { beforeEach, describe, expect, it } from "vitest";
import {
  clearTokenPair,
  getAccessToken,
  getRefreshToken,
  onSessionCleared,
  storeTokenPair,
} from "./token-store";

describe("token store", () => {
  beforeEach(() => {
    clearTokenPair();
  });

  it("keeps the access token in memory and the refresh token in the browser session", () => {
    storeTokenPair({ accessToken: "access-token", refreshToken: "refresh-token" });

    expect(getAccessToken()).toBe("access-token");
    expect(getRefreshToken()).toBe("refresh-token");
    expect(localStorage.length).toBe(0);
  });

  it("clears both tokens", () => {
    storeTokenPair({ accessToken: "access-token", refreshToken: "refresh-token" });

    clearTokenPair();

    expect(getAccessToken()).toBeNull();
    expect(getRefreshToken()).toBeNull();
  });

  it("notifies the authentication boundary when a session is invalidated", () => {
    let invalidated = false;
    const unsubscribe = onSessionCleared(() => {
      invalidated = true;
    });

    clearTokenPair();
    unsubscribe();

    expect(invalidated).toBe(true);
  });
});
