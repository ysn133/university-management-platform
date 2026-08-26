import { useAuth } from "@/features/auth/hooks/useAuth";
import { RootOverviewPage } from "@/features/root-governance/pages/RootOverviewPage";
import { EstablishmentWorkspacePage } from "@/features/establishment-management/pages/EstablishmentWorkspacePage";

export function ManagementOverviewPage() {
  const { user } = useAuth();

  if (user?.role === "ROOT_SUPER_ADMIN") {
    return <RootOverviewPage />;
  }

  return <EstablishmentWorkspacePage />;
}
