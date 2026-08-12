import { WorkspaceLayout } from "@/shared/layouts/WorkspaceLayout";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { professorNavigation } from "../navigation/professor-navigation";

export function ProfessorLayout() {
  const { user } = useAuth();

  return (
    <WorkspaceLayout
      accountPath="/professor/account/password"
      breadcrumbs={[{ label: "Professor Workspace" }]}
      context={{
        name: user ? `${user.firstName} ${user.lastName}` : "Professor Workspace",
        meta: "Teaching operations",
        monogram: user ? `${user.firstName[0]}${user.lastName[0]}` : "PR",
      }}
      navigation={professorNavigation}
      showGlobalRail={false}
      scopeLabel="Teaching operations"
      workspaceName="Professor"
      variant="management"
    />
  );
}
