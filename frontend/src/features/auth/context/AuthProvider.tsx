import { type PropsWithChildren, useEffect, useState } from "react";
import { queryClient } from "@/shared/api/query-client";
import * as authApi from "../api/auth-api";
import type { AuthenticatedUser } from "../model/auth-types";
import { clearTokenPair, onSessionCleared } from "../session/token-store";
import { AuthContext } from "./AuthContext";

export function AuthProvider({ children }: PropsWithChildren) {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [isRestoring, setIsRestoring] = useState(true);

  useEffect(
    () =>
      onSessionCleared(() => {
        queryClient.clear();
        setUser(null);
      }),
    [],
  );

  useEffect(() => {
    let active = true;

    authApi
      .restoreSession()
      .then((restoredUser) => {
        if (active) {
          setUser(restoredUser);
        }
      })
      .catch(() => {
        clearTokenPair();
        if (active) {
          setUser(null);
        }
      })
      .finally(() => {
        if (active) {
          setIsRestoring(false);
        }
      });

    return () => {
      active = false;
    };
  }, []);

  async function login(request: authApi.LoginRequest): Promise<AuthenticatedUser> {
    const authenticatedUser = await authApi.login(request);
    setUser(authenticatedUser);
    return authenticatedUser;
  }

  async function logout(): Promise<void> {
    try {
      await authApi.logout();
    } finally {
      queryClient.clear();
      setUser(null);
    }
  }

  return (
    <AuthContext
      value={{
        user,
        isRestoring,
        login,
        logout,
        changePassword: authApi.changePassword,
      }}
    >
      {children}
    </AuthContext>
  );
}
