import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import {
  getLoginPath,
  getWorkspacePath,
  portalRoles,
  type PortalType,
} from "../model/auth-types";
import { SessionLoadingPage } from "./SessionLoadingPage";

interface ProtectedRouteProps {
  portal: PortalType;
}

export function ProtectedRoute({ portal }: ProtectedRouteProps) {
  const { user, isRestoring } = useAuth();
  const location = useLocation();

  if (isRestoring) {
    return <SessionLoadingPage />;
  }

  if (!user) {
    return <Navigate replace state={{ returnTo: `${location.pathname}${location.search}` }} to={getLoginPath(portal)} />;
  }

  if (!portalRoles[portal].includes(user.role)) {
    return <Navigate replace to={getWorkspacePath(user.role)} />;
  }

  return <Outlet />;
}
