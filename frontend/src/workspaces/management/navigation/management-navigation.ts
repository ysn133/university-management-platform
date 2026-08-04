import type { WorkspaceNavigationItem } from "@/shared/layouts/WorkspaceLayout";
import type { AccountRole } from "@/features/auth/model/auth-types";

export function getManagementNavigation(
  role?: AccountRole,
  establishmentId?: string | null,
): WorkspaceNavigationItem[] {
  const isRoot = role === "ROOT_SUPER_ADMIN";
  const hasEstablishmentContext = Boolean(establishmentId) || role === "SUPER_ADMIN";

  if (hasEstablishmentContext) {
    const contextPath = isRoot
      ? `/management/establishments/${establishmentId}`
      : "/management";

    return [
      { label: "Overview", to: contextPath, end: true, icon: "overview", group: "Establishment" },
      ...(isRoot
        ? [
            { label: "Super Admins", to: `${contextPath}/super-admins`, icon: "leadership", group: "People and access" } as const,
          ]
        : []),
      ...(isRoot || role === "SUPER_ADMIN"
        ? [{
            label: "Admins",
            to: isRoot ? `${contextPath}/admins` : "/management/admins",
            icon: "admins",
            group: "People and access",
          } as const]
        : []),
    ];
  }

  const navigation: WorkspaceNavigationItem[] = [
    { label: "Overview", to: "/management", end: true, icon: "overview", group: "University" },
  ];

  if (isRoot) {
    navigation.push({ label: "Establishments", to: "/management/establishments", icon: "establishments", group: "University" });
  }

  return navigation;
}
