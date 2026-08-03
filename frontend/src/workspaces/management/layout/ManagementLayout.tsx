import { WorkspaceLayout } from "@/shared/layouts/WorkspaceLayout";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { getManagementNavigation } from "../navigation/management-navigation";

export function ManagementLayout() {
  const { user } = useAuth();

  return (
    <WorkspaceLayout
      navigation={getManagementNavigation(user?.role)}
      scopeLabel="University governance"
      variant="management"
      workspaceName="Management"
    />
  );
}
