import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { getWorkspacePath } from "../model/auth-types";
import { SessionLoadingPage } from "./SessionLoadingPage";

export function PublicOnlyRoute() {
  const { user, isRestoring } = useAuth();

  if (isRestoring) {
    return <SessionLoadingPage />;
  }

  return user ? <Navigate replace to={getWorkspacePath(user.role)} /> : <Outlet />;
}
