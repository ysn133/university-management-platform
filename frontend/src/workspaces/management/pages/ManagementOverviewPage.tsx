import { WorkspaceIntroduction } from "@/shared/components/WorkspaceIntroduction";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { RootOverviewPage } from "@/features/root-governance/pages/RootOverviewPage";
import { EstablishmentWorkspacePage } from "@/features/establishment-management/pages/EstablishmentWorkspacePage";

export function ManagementOverviewPage() {
  const { user } = useAuth();

  if (user?.role === "ROOT_SUPER_ADMIN") {
    return <RootOverviewPage />;
  }

  if (user?.role === "SUPER_ADMIN") {
    return <EstablishmentWorkspacePage />;
  }

  return (
    <WorkspaceIntroduction
      description="One workspace will serve Root Super Admin, Super Admin, and Admin. Navigation and actions will adapt to the authenticated user's authority."
      eyebrow="Management workspace"
      nextStep="Authentication and the Root governance workflow are implemented next."
      title="University operations, without duplicated dashboards."
    />
  );
}
