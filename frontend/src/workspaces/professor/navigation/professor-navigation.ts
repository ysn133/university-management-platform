import type { WorkspaceNavigationItem } from "@/shared/layouts/WorkspaceLayout";

export function getProfessorNavigation(hasModuleResponsibilities: boolean): WorkspaceNavigationItem[] {
  const navigation: WorkspaceNavigationItem[] = [
  { label: "Overview", to: "/professor", end: true, icon: "overview", group: "Professor workspace" },
  { label: "My Teaching", to: "/professor/teaching", icon: "programs", group: "Teaching" },
  { label: "Module Responsibilities", to: "/professor/modules", icon: "rules", group: "Teaching" },
  { label: "My Schedule", to: "/professor/schedule", icon: "years", group: "Teaching" },
  { label: "Attendance", to: "/professor/attendance", icon: "students", group: "Assessment" },
  ];

  if (hasModuleResponsibilities) {
    navigation.splice(4, 0,
      { label: "Exam Schedule", to: "/professor/exams", icon: "years", group: "Teaching" },
      { label: "Grades", to: "/professor/grades", icon: "rules", group: "Assessment" },
    );
  }

  return navigation;
}
