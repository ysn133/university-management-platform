import type { TokenPair } from "../model/auth-types";

const refreshTokenKey = "ysn-university.refresh-token";
let accessToken: string | null = null;
const sessionClearedListeners = new Set<() => void>();

function browserSessionStorage(): Storage | null {
  return typeof window === "undefined" ? null : window.sessionStorage;
}

export function getAccessToken(): string | null {
  return accessToken;
}

export function getRefreshToken(): string | null {
  return browserSessionStorage()?.getItem(refreshTokenKey) ?? null;
}

export function storeTokenPair(tokens: TokenPair): void {
  accessToken = tokens.accessToken;
  browserSessionStorage()?.setItem(refreshTokenKey, tokens.refreshToken);
}

export function clearTokenPair(): void {
  accessToken = null;
  browserSessionStorage()?.removeItem(refreshTokenKey);
  sessionClearedListeners.forEach((listener) => listener());
}

export function onSessionCleared(listener: () => void): () => void {
  sessionClearedListeners.add(listener);
  return () => sessionClearedListeners.delete(listener);
}
