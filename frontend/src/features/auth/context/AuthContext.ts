import { createContext } from "react";
import type { ChangePasswordRequest, LoginRequest } from "../api/auth-api";
import type { AuthenticatedUser } from "../model/auth-types";

export interface AuthContextValue {
  user: AuthenticatedUser | null;
  isRestoring: boolean;
  login: (request: LoginRequest) => Promise<AuthenticatedUser>;
  logout: () => Promise<void>;
  changePassword: (request: ChangePasswordRequest) => Promise<void>;
}

export const AuthContext = createContext<AuthContextValue | null>(null);
