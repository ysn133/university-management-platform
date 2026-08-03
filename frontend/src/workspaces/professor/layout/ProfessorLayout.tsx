import { WorkspaceLayout } from "@/shared/layouts/WorkspaceLayout";
import { professorNavigation } from "../navigation/professor-navigation";

export function ProfessorLayout() {
  return (
    <WorkspaceLayout
      navigation={professorNavigation}
      scopeLabel="Teaching operations"
      workspaceName="Professor"
    />
  );
}
