import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "@/features/auth/hooks/useAuth";

export function EstablishmentOperationRoute() {
  const { user } = useAuth();

  if (!user || !["SUPER_ADMIN", "ADMIN"].includes(user.role)) {
    return <Navigate replace to="/management" />;
  }

  return <Outlet />;
}
