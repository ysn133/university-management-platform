import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "@/features/auth/hooks/useAuth";

export function RootOnlyRoute() {
  const { user } = useAuth();
  return user?.role === "ROOT_SUPER_ADMIN" ? <Outlet /> : <Navigate replace to="/management" />;
}
