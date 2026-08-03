import type { WorkspaceNavigationItem } from "@/shared/layouts/WorkspaceLayout";
import type { AccountRole } from "@/features/auth/model/auth-types";

export function getManagementNavigation(role?: AccountRole): WorkspaceNavigationItem[] {
  const navigation: WorkspaceNavigationItem[] = [
    { label: "Overview", to: "/management", end: true },
  ];

  if (role === "ROOT_SUPER_ADMIN") {
    navigation.push({ label: "Establishments", to: "/management/establishments" });
  }

  return navigation;
}
