import { Navigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { getWorkspacePath } from "../model/auth-types";
import { SessionLoadingPage } from "./SessionLoadingPage";

export function HomeRedirect() {
  const { user, isRestoring } = useAuth();

  if (isRestoring) {
    return <SessionLoadingPage />;
  }

  return <Navigate replace to={user ? getWorkspacePath(user.role) : "/management/login"} />;
}
