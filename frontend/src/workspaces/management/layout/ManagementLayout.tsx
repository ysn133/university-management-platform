import { WorkspaceLayout } from "@/shared/layouts/WorkspaceLayout";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { getEstablishment, rootGovernanceKeys } from "@/features/root-governance/api/root-governance-api";
import { useQuery } from "@tanstack/react-query";
import { useLocation } from "react-router-dom";
import { getManagementNavigation } from "../navigation/management-navigation";

export function ManagementLayout() {
  const { user } = useAuth();
  const location = useLocation();
  const routeEstablishmentId = location.pathname.match(
    /^\/management\/establishments\/([^/]+)/,
  )?.[1];
  const establishmentId = routeEstablishmentId ?? (
    user?.role === "SUPER_ADMIN" || user?.role === "ADMIN"
      ? user.establishmentId
      : null
  );
  const establishmentQuery = useQuery({
    queryKey: rootGovernanceKeys.establishment(establishmentId ?? "missing"),
    queryFn: () => getEstablishment(establishmentId!),
    enabled: Boolean(establishmentId),
  });
  const establishment = establishmentQuery.data;
  const contextPath = user?.role === "ROOT_SUPER_ADMIN" && establishmentId
    ? `/management/establishments/${establishmentId}`
    : "/management";
  const adminDetailMatch = location.pathname.match(/\/admins\/([^/]+)$/);
  const section = location.pathname.endsWith("/super-admins")
      ? "Super Admins"
      : location.pathname.endsWith("/admins")
        ? "Admins"
        : null;
  const adminsPath = user?.role === "ROOT_SUPER_ADMIN"
    ? `${contextPath}/admins`
    : "/management/admins";
  const breadcrumbs = establishmentId
    ? [
        ...(user?.role === "ROOT_SUPER_ADMIN"
          ? [
              { label: "Université Ibn Zohr", to: "/management" },
              { label: "Establishments", to: "/management/establishments" },
            ]
          : []),
        { label: establishment?.name ?? "Establishment", to: contextPath },
        ...(section ? [{ label: section }] : []),
        ...(adminDetailMatch
          ? [
              { label: "Admins", to: adminsPath },
              { label: "Admin details" },
            ]
          : []),
      ]
    : [];

  return (
    <WorkspaceLayout
      breadcrumbs={breadcrumbs}
      context={establishmentId ? {
        name: establishment?.name ?? "Loading establishment...",
        meta: establishment
          ? `${establishment.type[0] + establishment.type.slice(1).toLowerCase()} · ${establishment.status}`
          : "Establishment context",
        monogram: establishment?.name.slice(0, 2).toUpperCase() ?? "--",
        backLabel: user?.role === "ROOT_SUPER_ADMIN" ? "Back to establishments" : undefined,
        backTo: user?.role === "ROOT_SUPER_ADMIN" ? "/management/establishments" : undefined,
      } : {
        name: "Université Ibn Zohr",
        meta: "University governance",
        monogram: "UIZ",
      }}
      navigation={getManagementNavigation(user?.role, establishmentId)}
      scopeLabel={establishmentId ? "Establishment operations" : "University governance"}
      variant="management"
      workspaceName="Management"
    />
  );
}
