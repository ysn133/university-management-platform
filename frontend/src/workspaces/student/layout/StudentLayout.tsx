import { WorkspaceLayout } from "@/shared/layouts/WorkspaceLayout";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { studentNavigation } from "../navigation/student-navigation";

export function StudentLayout() {
  const { user } = useAuth();

  return (
    <WorkspaceLayout
      accountPath="/student/account"
      breadcrumbs={[{ label: "Student Workspace" }]}
      context={{
        name: user ? `${user.firstName} ${user.lastName}` : "Student Workspace",
        meta: "Personal academic services",
        monogram: user ? `${user.firstName[0]}${user.lastName[0]}` : "ST",
      }}
      navigation={studentNavigation}
      scopeLabel="Personal academic record"
      showGlobalRail={false}
      variant="management"
      workspaceName="Student"
    />
  );
}
