import { WorkspaceLayout } from "@/shared/layouts/WorkspaceLayout";
import { managementNavigation } from "../navigation/management-navigation";

export function ManagementLayout() {
  return (
    <WorkspaceLayout
      navigation={managementNavigation}
      scopeLabel="University governance"
      workspaceName="Management"
    />
  );
}
