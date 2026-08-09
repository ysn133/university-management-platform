import type { WorkspaceNavigationItem } from "@/shared/layouts/WorkspaceLayout";
import type { AccountRole } from "@/features/auth/model/auth-types";

export function getManagementNavigation(
  role?: AccountRole,
  establishmentId?: string | null,
): WorkspaceNavigationItem[] {
  const isRoot = role === "ROOT_SUPER_ADMIN";
  const hasEstablishmentContext = Boolean(establishmentId) || role === "SUPER_ADMIN" || role === "ADMIN";

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
      { label: "Students", to: `${contextPath}/students`, icon: "students", group: "People and access" },
      { label: "Departments", to: `${contextPath}/departments`, icon: "departments", group: "Academic structure" },
      { label: "Program Paths", to: `${contextPath}/program-paths`, icon: "paths", group: "Academic structure" },
      { label: "Degree Cycles", to: `${contextPath}/degree-cycles`, icon: "cycles", group: "Academic structure" },
      { label: "Programs", to: `${contextPath}/programs`, icon: "programs", group: "Academic structure" },
      { label: "Academic Years", to: `${contextPath}/academic-years`, icon: "years", group: "Academic structure" },
      { label: "Academic Rules", to: `${contextPath}/academic-rule-profiles`, icon: "rules", group: "Academic structure" },
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
