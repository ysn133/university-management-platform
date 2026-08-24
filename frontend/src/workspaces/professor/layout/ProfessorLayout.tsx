import { WorkspaceLayout } from "@/shared/layouts/WorkspaceLayout";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { useQuery } from "@tanstack/react-query";
import { getMyModuleResponsibilities, professorOverviewKeys } from "../api/professor-overview-api";
import { getProfessorNavigation } from "../navigation/professor-navigation";

export function ProfessorLayout() {
  const { user } = useAuth();
  const responsibilitiesQuery = useQuery({
    queryKey: professorOverviewKeys.responsibilities(),
    queryFn: getMyModuleResponsibilities,
  });
  const hasModuleResponsibilities = (responsibilitiesQuery.data ?? []).some((responsibility) => responsibility.status === "ACTIVE");

  return (
    <WorkspaceLayout
      accountPath="/professor/account"
      breadcrumbs={[{ label: "Professor Workspace" }]}
      context={{
        name: user ? `${user.firstName} ${user.lastName}` : "Professor Workspace",
        meta: "Teaching operations",
        monogram: user ? `${user.firstName[0]}${user.lastName[0]}` : "PR",
      }}
      navigation={getProfessorNavigation(hasModuleResponsibilities)}
      showGlobalRail={false}
      scopeLabel="Teaching operations"
      workspaceName="Professor"
      variant="management"
    />
  );
}
