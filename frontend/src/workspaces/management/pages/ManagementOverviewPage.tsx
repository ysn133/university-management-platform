import { WorkspaceIntroduction } from "@/shared/components/WorkspaceIntroduction";

export function ManagementOverviewPage() {
  return (
    <WorkspaceIntroduction
      description="One workspace will serve Root Super Admin, Super Admin, and Admin. Navigation and actions will adapt to the authenticated user's authority."
      eyebrow="Management workspace"
      nextStep="Authentication and the Root governance workflow are implemented next."
      title="University operations, without duplicated dashboards."
    />
  );
}
