import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "@/features/auth/hooks/useAuth";

export function EstablishmentAuthorityRoute() {
  const { user } = useAuth();

  if (user?.role !== "ROOT_SUPER_ADMIN" && user?.role !== "SUPER_ADMIN") {
    return <Navigate replace to="/management" />;
  }

  return <Outlet />;
}
