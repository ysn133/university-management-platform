import { WorkspaceIntroduction } from "@/shared/components/WorkspaceIntroduction";

export function StudentOverviewPage() {
  return (
    <WorkspaceIntroduction
      description="Published schedules, exams, grades, attendance, and academic history will be available through student-specific self-service APIs."
      eyebrow="Student workspace"
      nextStep="Student self-service is delivered after the management and Professor workflows."
      title="Academic information that remains clear over time."
    />
  );
}
