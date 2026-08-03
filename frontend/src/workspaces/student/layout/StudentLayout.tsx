import { WorkspaceLayout } from "@/shared/layouts/WorkspaceLayout";
import { studentNavigation } from "../navigation/student-navigation";

export function StudentLayout() {
  return (
    <WorkspaceLayout
      navigation={studentNavigation}
      scopeLabel="Personal academic record"
      workspaceName="Student"
    />
  );
}
